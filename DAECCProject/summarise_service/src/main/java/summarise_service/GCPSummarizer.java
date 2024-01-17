package summarise_service;
import com.google.cloud.documentai.v1beta3.DocumentProcessorServiceSettings;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.documentai.v1beta3.Document;
import com.google.cloud.documentai.v1beta3.DocumentProcessorServiceClient;
import com.google.cloud.documentai.v1beta3.ProcessRequest;
import com.google.cloud.documentai.v1beta3.ProcessResponse;
import com.google.cloud.documentai.v1beta3.RawDocument;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GCPSummarizer implements SummarizeService {

    public SummarizerResponse summarize(String filename) {
        GCPConfig config = LoadConfig();
        return processDocument(config.projectId, config.location, config.processorId,config.filePath);
    }

    public SummarizerResponse processDocument(String projectId, String location, String processorId, String filePath) {
        String endpoint = String.format("%s-documentai.googleapis.com:443", location);
        DocumentProcessorServiceSettings settings;
        try{
            settings =
            DocumentProcessorServiceSettings.newBuilder().setEndpoint(endpoint).build();
        } catch (Exception e) {
            // TODO: handle exception
            return null;
        }

        try (DocumentProcessorServiceClient client = DocumentProcessorServiceClient.create(settings)) {
            String processorName = String.format("projects/%s/locations/%s/processors/%s", projectId, location, processorId);

            byte[] pdfContent = Files.readAllBytes(Paths.get(filePath));
            ByteString content = ByteString.copyFrom(pdfContent);

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

    private GCPConfig LoadConfig() {
        ObjectMapper mapper = new ObjectMapper();
        GCPConfig config = null;
        try {
            config = mapper.readValue(this.getClass().getResourceAsStream("config.json"), GCPConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config;
    }
}