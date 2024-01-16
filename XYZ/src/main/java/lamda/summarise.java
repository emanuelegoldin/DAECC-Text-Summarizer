package lamda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class summarise implements RequestHandler<SummariseInput, SummariseOutput>, HttpFunction {
    public SummariseOutput handleRequest(SummariseInput summariseInput, Context context) {
        return null;
    }

    public void service(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {
        JsonObject body = Gson.fromJson(httpRequest.getReader(), JsonObject.class);
        SummariseInput input = Gson.fromJson(body.toString(), SummariseInput.class);
        SummariseOutput output = exec(input);
        response.getWriter().write(gson.toJson(output));
    }

    private SummariseOutput exec()
}
