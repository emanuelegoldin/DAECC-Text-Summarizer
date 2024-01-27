package summarise_service;
import java.io.InputStream;
import java.util.Collections;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.documentai.v1.DocumentProcessorServiceSettings;
import com.google.cloud.documentai.v1.Document;
import com.google.cloud.documentai.v1.DocumentProcessorServiceClient;
import com.google.cloud.documentai.v1.ProcessRequest;
import com.google.cloud.documentai.v1.ProcessResponse;
import com.google.cloud.documentai.v1.RawDocument;
import com.google.protobuf.ByteString;

import shared.Credentials;


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
            
            String processorName = System.getenv("PROCESSOR_NAME");

            Document rawDocument = Document.newBuilder()
                .setContent(ByteString.copyFromUtf8(content))
                .setMimeType("text/plain")
                .build();

            ProcessRequest request = ProcessRequest.newBuilder()
                .setName(processorName)
                
                .setInlineDocument(rawDocument)
                .build();

            ProcessResponse response = client.processDocument(request);
            Document document = response.getDocument();

            String summarizedText = document.getEntities(0).getNormalizedValue().getText();
            System.out.println("summarized text: " + summarizedText);
            SummarizerResponse summarizerResponse = new SummarizerResponse();
            summarizerResponse.summary = summarizedText;  
            return summarizerResponse;
        } catch (Exception e) {
            throw new RuntimeException("Failed to process document: " + e.getMessage());
        }
    }
}