from google.cloud import documentai
from google.cloud import storage
import os

OUTPUT_BUCKET_NAME = os.environ.get('OUTPUT_BUCKET_NAME')
processor_full_id = os.environ.get('PROCESSOR_ID')
processor_id_parts = processor_full_id.split('/')
PROCESSOR_ID = processor_id_parts[-1]

def main(data, context):
    bucket_name = data['bucket']
    file_name = data['name']
    print(PROCESSOR_ID + OUTPUT_BUCKET_NAME)
    print(f"Processing document: {file_name} in bucket {bucket_name}")
    storage_client = storage.Client()

    summarized_text = process_arrival_file(storage_client, bucket_name, file_name)
    save_document_to_bucket(storage_client, file_name, summarized_text)


def process_arrival_file(storage_client, bucket_name, file_name):
    bucket = storage_client.get_bucket(bucket_name)
    blob = bucket.blob(file_name)

    # Download the file content from Cloud Storage
    pdf_content = blob.download_as_string()

    # Create a Document AI client
    client = documentai.DocumentProcessorServiceClient()
    # Load binary data

    raw_document = documentai.RawDocument(
        content=pdf_content,
        mime_type='application/pdf'
    )

    processor = client.processor_path("summarization-project-406313", "us", PROCESSOR_ID)

    request = documentai.ProcessRequest(name=processor, raw_document=raw_document)
    document = client.process_document(request=request)

    data_text = str(document.document.entities)
    start_index = data_text.find('mention_text:') + len('mention_text:')
    end_index = data_text.find('normalized_value', start_index)

    extracted_text = data_text[start_index:end_index].strip()
    extracted_text = extracted_text.replace('•', '')

    print('extracted text: ' + extracted_text)

    return extracted_text


def save_document_to_bucket(storage_client, file_name, text):
    output_bucket = storage_client.get_bucket(OUTPUT_BUCKET_NAME)

    summarized_file_name = file_name[:-4] + '_summarized.txt'

    blob = output_bucket.blob(summarized_file_name)

    # Upload the extracted text as a string to a file in the bucket
    blob.upload_from_string(text, content_type='text/plain')

    print(f"Extracted text uploaded to {output_bucket}/{summarized_file_name}")
