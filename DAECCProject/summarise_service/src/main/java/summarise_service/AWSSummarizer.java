package summarise_service;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.amazonaws.services.sagemakerruntime.AmazonSageMakerRuntime;
import com.amazonaws.services.sagemakerruntime.AmazonSageMakerRuntimeClientBuilder;
import com.amazonaws.services.sagemakerruntime.model.InvokeEndpointRequest;
import com.amazonaws.services.sagemakerruntime.model.InvokeEndpointResult;

public class AWSSummarizer implements SummarizeService {
    private AmazonSageMakerRuntime sagemakerRuntime;

    public AWSSummarizer() {
        this.sagemakerRuntime = AmazonSageMakerRuntimeClientBuilder.defaultClient();
    }

    public AWSSummarizer(Object testing) {
    }

    public SummarizerResponse summarize(String inputFile) {

        // Read endpoint
        String endpoint = System.getenv("ENDPOINT_NAME");
        
        InvokeEndpointRequest invokeEndpointRequest = new InvokeEndpointRequest()
            .withEndpointName(endpoint)
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
}
