package summarise_service;
import com.google.cloud.documentai.v1beta3.DocumentProcessorServiceSettings;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.documentai.v1beta3.Document;
import com.google.cloud.documentai.v1beta3.DocumentProcessorServiceClient;
import com.google.cloud.documentai.v1beta3.ProcessRequest;
import com.google.cloud.documentai.v1beta3.ProcessResponse;
import com.google.cloud.documentai.v1beta3.RawDocument;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.StorageOptions;
import com.google.protobuf.ByteString;

import shared.Credentials;

import java.net.URI;
import java.net.URISyntaxException;

// CORE library
// import shared.Credentials;
// import storage.Storage;
// import storage.StorageImpl;

public class GCPSummarizer implements SummarizeService {

    public SummarizerResponse summarize(String filename) {
        return processDocument(filename);
    }

    public SummarizerResponse processDocument(String filePath) {
        String endpoint = String.format("%s-documentai.googleapis.com:443", "us");
        Credentials credentials;
        try {
            credentials = Credentials.loadDefaultCredentials();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load credentials: " + e.getMessage());
        }
        DocumentProcessorServiceSettings settings;
        try{
            GoogleCredentials googleCredentials = credentials.getGcpCredentials();
            CredentialsProvider credentialsProvider = FixedCredentialsProvider.create(googleCredentials);

            settings = DocumentProcessorServiceSettings.newBuilder()
                .setEndpoint(endpoint)
                .setCredentialsProvider(credentialsProvider)
                .build();
        } catch (Exception e) {
            // TODO: handle exception
            return null;
        }

        try (DocumentProcessorServiceClient client = DocumentProcessorServiceClient.create(settings)) {
            
            String processorName = System.getenv("PROCESSOR_NAME");

            ByteString content = fetchFileFromStorage(filePath);
            RawDocument rawDocument = RawDocument.newBuilder()
                .setContent(content)
                .setMimeType("application/pdf")
                .build();

            ProcessRequest request = ProcessRequest.newBuilder()
                .setName(processorName)
                .setRawDocument(rawDocument)
                .build();

            ProcessResponse response = client.processDocument(request);
            Document document = response.getDocument();

            String summarizedText = document.getEntities(0).getNormalizedValue().getText();
            System.out.println("summarized text: " + summarizedText);
            SummarizerResponse summarizerResponse = new SummarizerResponse();
            summarizerResponse.summary = summarizedText;  
            return summarizerResponse;
        } catch (Exception e) {
            // TODO: handle exception
            return null;
        }
    }

    private  ByteString fetchFileFromStorage(String fileLocation) throws URISyntaxException {
        URI uri = new URI(fileLocation);
        String bucketName = uri.getHost();
        String fileName = uri.getPath().substring(1);
        Storage storage = StorageOptions.getDefaultInstance().getService();
        Blob blob = storage.get(bucketName, fileName);
        return ByteString.copyFrom(blob.getContent());
    }

}