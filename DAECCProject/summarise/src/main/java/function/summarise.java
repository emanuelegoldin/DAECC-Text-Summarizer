package function;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import summarise_service.Provider;
import summarise_service.SummarizeService;
import summarise_service.SummarizerFactory;
import summarise_service.SummarizerResponse;

public class summarise implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>, HttpFunction {
    private static final Gson gson = new Gson();

    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        try {
            SummariseInput summariseInput = gson.fromJson(event.getBody(), SummariseInput.class);
            SummariseOutput output = exec(summariseInput);

            APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
            responseEvent.setStatusCode(200);
            responseEvent.setBody(gson.toJson(output));
            return responseEvent;
        } catch (Exception e) {
            // Handle exception
            return null;
        }
    }

    public void service(HttpRequest request, HttpResponse response) throws Exception {
        // Parse request body into SummariseInput object
        JsonObject body = gson.fromJson(request.getReader(), JsonObject.class);
        SummariseInput input = gson.fromJson(body.toString(), SummariseInput.class);
        
        String fileLocation = input.inputFile;
        System.out.println(fileLocation);

        if (fileLocation == null || fileLocation.isEmpty()) {
            response.setStatusCode(400);
            response.getWriter().write("Missing 'location' parameter");
            return;
        }
        try {
            //todo: pass config here , or load it inside
            SummariseOutput output = exec(input);
            response.getWriter().write(output.getSummary());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.getWriter().write("Error executing function: " + e.getMessage());
        }

    }

    private SummariseOutput exec(SummariseInput input) {
        System.out.println("Input: " + input.inputFile + " " + input.provider);
        Provider provider = StringToProvider(input.provider);
        SummarizeService service = SummarizerFactory.Create(provider);
        SummarizerResponse summary = service.summarize(input.inputFile);
        return new SummariseOutput().summary(summary.summary);
    }

    private Provider StringToProvider(String provider) {
        switch (provider) {
            case "AWS":
                return Provider.AWS;
            case "GCP":
            default:
                return Provider.GCP;
        }
    }
}
