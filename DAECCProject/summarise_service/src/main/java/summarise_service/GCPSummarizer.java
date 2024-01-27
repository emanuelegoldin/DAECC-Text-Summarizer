package summarise_service;
import com.google.cloud.documentai.v1beta3.DocumentProcessorServiceSettings;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.cloud.documentai.v1beta3.Document;
import com.google.cloud.documentai.v1beta3.DocumentProcessorServiceClient;
import com.google.cloud.documentai.v1beta3.ProcessRequest;
import com.google.cloud.documentai.v1beta3.ProcessResponse;
import com.google.cloud.documentai.v1beta3.RawDocument;
import com.google.protobuf.ByteString;

import shared.Credentials;

import java.util.Optional;

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
        Credentials credentials;
        try {
            credentials = Credentials.loadDefaultCredentials();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load credentials: " + e.getMessage());
        }
        DocumentProcessorServiceSettings settings;
        try{
            settings = DocumentProcessorServiceSettings.newBuilder()
                .setEndpoint(endpoint)
                .setCredentialsProvider(FixedCredentialsProvider.create((credentials.getGcpCredentials())))
                .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load credentials: " + e.getMessage());
        }

        try (DocumentProcessorServiceClient client = DocumentProcessorServiceClient.create(settings)) {
            
            String processorName = System.getenv("PROCESSOR_NAME");

            RawDocument rawDocument = RawDocument.newBuilder()
                .setContent(ByteString.copyFromUtf8(content))
                .setMimeType("text/plain")
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
            throw new RuntimeException("Failed to process document: " + e.getMessage());
        }
    }
}