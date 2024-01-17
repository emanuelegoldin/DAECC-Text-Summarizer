package summarise_service;
import com.google.cloud.documentai.v1beta3.Document;
import com.google.cloud.documentai.v1beta3.DocumentProcessorServiceClient;
import com.google.cloud.documentai.v1beta3.ProcessRequest;
import com.google.cloud.documentai.v1beta3.RawDocument;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.IOException;
public class GCPSummarizer implements SummarizeService {
    public SummarizerResponse summarize(String inputFile) {

        try {
            DocumentProcessorServiceClient  client = DocumentProcessorServiceClient.create();

            // Load binary data
            RawDocument rawDocument = RawDocument.newBuilder()
                    //.setContent(com.google.protobuf.ByteString.copyFrom(pdfContent))
                    .setMimeType("application/pdf")
                    .build();

            String processorName = String.format(
                    "projects/%s/locations/us/processors/%s",
                    "summarization-project-406313", "PROCESSOR_ID"
            );

            ProcessRequest request = ProcessRequest.newBuilder()
                    .setName(processorName)
                    .setRawDocument(rawDocument)
                    .build();

            Document document = client.processDocument(request).getDocument();

            String dataText = document.getEntitiesList().toString();
            int startIndex = dataText.indexOf("mention_text:") + "mention_text:".length();
            int endIndex = dataText.indexOf("normalized_value", startIndex);

            String extractedText = dataText.substring(startIndex, endIndex).trim().replace("•", "");

            System.out.println("Extracted text: " + extractedText);
        }catch (Exception e){}
        return null;
    }
}



//public class DocumentProcessor {
//
//    private static final String OUTPUT_BUCKET_NAME = System.getenv("OUTPUT_BUCKET_NAME");
//    private static final String PROCESSOR_ID = System.getenv("PROCESSOR_ID");
//
//    public static void main(String[] args) {
//        String bucketName = args[0];
//        String fileName = args[1];
//
//        System.out.println(PROCESSOR_ID + OUTPUT_BUCKET_NAME);
//        System.out.printf("Processing document: %s in bucket %s%n", fileName, bucketName);
//
//        Storage storage = StorageOptions.getDefaultInstance().getService();
//        String summarizedText = processArrivalFile(storage, bucketName, fileName);
//        saveDocumentToBucket(storage, fileName, summarizedText);
//    }
//
//    private static String processArrivalFile(Storage storage, String bucketName, String fileName) {
//        Blob blob = storage.get(bucketName, fileName);
//
//        // Download the file content from Cloud Storage
//        byte[] pdfContent = blob.getContent();
//
//        // Create a Document AI client
//        DocumentProcessorServiceClient client = DocumentProcessorServiceClient.create() {
//            // Load binary data
//            RawDocument rawDocument = RawDocument.newBuilder()
//                    .setContent(com.google.protobuf.ByteString.copyFrom(pdfContent))
//                    .setMimeType("application/pdf")
//                    .build();
//
//            String processorName = String.format(
//                    "projects/%s/locations/us/processors/%s",
//                    "summarization-project-406313", PROCESSOR_ID
//            );
//
//            ProcessRequest request = ProcessRequest.newBuilder()
//                    .setName(processorName)
//                    .setRawDocument(rawDocument)
//                    .build();
//
//            Document document = client.processDocument(request).getDocument();
//
//            String dataText = document.getEntitiesList().toString();
//            int startIndex = dataText.indexOf("mention_text:") + "mention_text:".length();
//            int endIndex = dataText.indexOf("normalized_value", startIndex);
//
//            String extractedText = dataText.substring(startIndex, endIndex).trim().replace("•", "");
//
//            System.out.println("Extracted text: " + extractedText);
//
//            return extractedText;
//
//        }
//
//        private void saveDocumentToBucket (Storage storage, String fileName, String text){
//            String summarizedFileName = fileName.substring(0, fileName.length() - 4) + "_summarized.txt";
//
//            Blob blob = storage.get(OUTPUT_BUCKET_NAME, summarizedFileName);
//
//            // Upload the extracted text as a string to a file in the bucket
//            blob.uploadFromByteArray(text.getBytes(), 0, text.length());
//
//            System.out.printf("Extracted text uploaded to %s/%s%n", OUTPUT_BUCKET_NAME, summarizedFileName);
//        }
//    }
//}