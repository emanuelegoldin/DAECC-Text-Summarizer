package summarise_service;
import java.io.InputStream;
import java.util.Collections;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.documentai.v1.DocumentProcessorServiceSettings;
import com.google.cloud.documentai.v1.Document;
import com.google.cloud.documentai.v1.DocumentProcessorServiceClient;
import com.google.cloud.documentai.v1.ProcessRequest;
import com.google.cloud.documentai.v1.ProcessResponse;
import com.google.protobuf.ByteString;



// CORE library
// import shared.Credentials;
// import storage.Storage;
// import storage.StorageImpl;

public class GCPSummarizer implements SummarizeService {

    public SummarizerResponse summarize(String content) {
        return processDocument(content);
    }

    public SummarizerResponse processDocument(String content) {
        String endpoint = String.format("%s-documentai.googleapis.com:443", "us");
        GoogleCredentials credentials;
        try {
            InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("key.json");
            credentials = GoogleCredentials.fromStream(in).createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"));;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load credentials: " + e.getMessage());
        }
        DocumentProcessorServiceSettings settings;
        
        try{
            settings = DocumentProcessorServiceSettings.newBuilder()
                .setEndpoint(endpoint)
                .setCredentialsProvider(() -> credentials)
                .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load credentials: " + e.getMessage());
        }

        try (DocumentProcessorServiceClient client = DocumentProcessorServiceClient.create(settings)) {
            System.out.println("Getting processor name...");
            String processorName = System.getenv("PROCESSOR_NAME");
            if (processorName == null) {
                throw new RuntimeException("Missing processor name. Set environment variable PROCESSOR_NAME.");
            }
            // Clean up the processor name due to passing it from terraform output
            processorName = processorName.replaceAll("[^a-zA-Z0-9-\\/]", "");

            Document rawDocument = Document.newBuilder()
                .setContent(ByteString.copyFromUtf8(content))
                .setMimeType("text/plain")
                .build();

            ProcessRequest request = ProcessRequest.newBuilder()
                .setName(processorName)
                .setInlineDocument(rawDocument)
                .build();

            long startTime = System.currentTimeMillis();
            ProcessResponse response = client.processDocument(request);
            long endTime = System.currentTimeMillis();
            Document document = response.getDocument();

            String summarizedText = document.getEntities(0).getNormalizedValue().getText();

            return SummarizerResponse.builder()
                .summary(summarizedText)
                .executionTime(endTime - startTime)
                .provider("GCP")
                .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to process document: " + e.getMessage());
        }
    }
}