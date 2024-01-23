package function;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import summarise_service.Provider;
import summarise_service.SummarizeService;
import summarise_service.SummarizerFactory;
import summarise_service.SummarizerResponse;

public class summarise implements RequestHandler<SummariseInput, SummariseOutput>, HttpFunction {
    private static final Gson gson = new Gson();

    public SummariseOutput handleRequest(SummariseInput summariseInput, Context context) {
        try {
            return exec(summariseInput);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void service(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {
        String fileLocation = request.getFirstQueryParameter("location").orElse(null);
        System.out.println(fileLocation);

        if (fileLocation == null || fileLocation.isEmpty()) {
            response.setStatusCode(400);
            response.getWriter().write("Missing 'location' parameter");
            return;
        }
        try {
            //todo: pass config here , or load it inside
            String resumedText =  processDocument(fileLocation);
            response.getWriter().write(resumedText);
        } catch (Exception e) {
            response.setStatusCode(500);
            response.getWriter().write("Error executing function: " + e.getMessage());
        }

    }

    private SummariseOutput exec(SummariseInput input) {
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
