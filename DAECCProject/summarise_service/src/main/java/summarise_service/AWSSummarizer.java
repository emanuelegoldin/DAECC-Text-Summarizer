package summarise_service;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.amazonaws.services.sagemakerruntime.AmazonSageMakerRuntime;
import com.amazonaws.services.sagemakerruntime.AmazonSageMakerRuntimeClientBuilder;
import com.amazonaws.services.sagemakerruntime.model.InvokeEndpointRequest;
import com.amazonaws.services.sagemakerruntime.model.InvokeEndpointResult;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AWSSummarizer implements SummarizeService {
    private AmazonSageMakerRuntime sagemakerRuntime;

    public AWSSummarizer() {
        this.sagemakerRuntime = AmazonSageMakerRuntimeClientBuilder.defaultClient();
    }

    public AWSSummarizer(Object testing) {
    }

    public SummarizerResponse summarize(String inputFile) {

        // Read endpoint
        AWSConfig config = readConfig();

        System.out.println("Endpoint: " + config.EndpointName);
        System.out.println("Text: " + config.Text);
        System.out.println("Input file: " + inputFile);
        
        InvokeEndpointRequest invokeEndpointRequest = new InvokeEndpointRequest()
            .withEndpointName(config.EndpointName)
            .withContentType("application/x-text")
            .withBody(ByteBuffer.wrap(inputFile.getBytes()));

        InvokeEndpointResult result = sagemakerRuntime.invokeEndpoint(invokeEndpointRequest);

        // Process the result
        ByteBuffer response = result.getBody();
        // Convert ByteBuffer to String
        String responseString = new String(response.array(), Charset.forName("UTF-8"));

        // TODO: store result in a file in a bucket
        SummarizerResponse summarizerResponse = new SummarizerResponse();
        summarizerResponse.summary = responseString;
        return summarizerResponse;
    }

    public AWSConfig readConfig() {
        ObjectMapper objectMapper = new ObjectMapper();
        AWSConfig config = new AWSConfig();
        try{
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.json");
            config = objectMapper.readValue(inputStream, AWSConfig.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return config;
    }
}
