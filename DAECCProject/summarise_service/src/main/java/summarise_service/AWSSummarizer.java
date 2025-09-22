package summarise_service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.json.JSONObject;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.sagemakerruntime.SageMakerRuntimeClient;
import software.amazon.awssdk.services.sagemakerruntime.model.InvokeEndpointRequest;
import software.amazon.awssdk.services.sagemakerruntime.model.InvokeEndpointResponse;
import software.amazon.awssdk.regions.Region;


// CORE library
import shared.Credentials;

public class AWSSummarizer implements SummarizeService {

    public AWSSummarizer() {}

    public SummarizerResponse summarize(String inputFile) {

        // Read endpoint
        String endpoint = System.getenv("ENDPOINT_NAME");

        // Read credentials
        Credentials credentials;
        try {
            credentials = Credentials.loadDefaultCredentials();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load credentials: " + e.getMessage());
        }
        
        // Create SageMaker client
        SageMakerRuntimeClient sageMakerRuntimeClient = SageMakerRuntimeClient.builder()
            .credentialsProvider(credentials.getAwsCredentials())
            .region(Region.US_EAST_1)
            .build();

        // Invoke SageMaker endpoint
        InvokeEndpointRequest invokeEndpointRequest = InvokeEndpointRequest.builder()
            .endpointName(endpoint)
            .contentType("application/x-text")
            .body(SdkBytes.fromByteBuffer(ByteBuffer.wrap(inputFile.getBytes())))
            .build();

        long startTime = System.currentTimeMillis();
        InvokeEndpointResponse result = sageMakerRuntimeClient.invokeEndpoint(invokeEndpointRequest);
        long endTime = System.currentTimeMillis();
        // Process the result
        ByteBuffer response = result.body().asByteBuffer();
        // Convert ByteBuffer to String
        byte[] responseBytes;
        if(response.hasArray()) {
            responseBytes = response.array();
        } else {
            responseBytes = new byte[response.remaining()];
            response.get(responseBytes);
        }
        String responseString = new String(responseBytes, Charset.forName("UTF-8"));
        JSONObject jsonResponse = new JSONObject(responseString);
        String summaryText = jsonResponse.getString("summary_text");

        return SummarizerResponse.builder()
            .summary(summaryText)
            .executionTime(endTime - startTime)
            .provider("AWS")
            .build();
    }
}
