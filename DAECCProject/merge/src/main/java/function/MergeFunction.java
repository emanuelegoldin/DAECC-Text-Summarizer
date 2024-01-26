package function;

import java.io.ByteArrayOutputStream;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import shared.Credentials;
import storage.Storage;
import storage.StorageImpl;

public class MergeFunction implements HttpFunction, RequestHandler<MergeInput, MergeOutput>{

    private static final Gson gson = new Gson();

    @Override
    public MergeOutput handleRequest(MergeInput input, Context context) {
        try {
            return exec(input);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {
        JsonObject body = gson.fromJson(request.getReader(), JsonObject.class);
        MergeInput input = gson.fromJson(body.toString(), MergeInput.class);
        MergeOutput output = exec(input);
        response.getWriter().write(gson.toJson(output));
    }

    private MergeOutput exec(MergeInput input) throws Exception {
        Credentials credentials;
        try {
            credentials = Credentials.loadDefaultCredentials();
        } catch (Exception e) {
            throw new RuntimeException("Error while parsing credentials: " + e.getMessage());
        }
        
        Storage storage = new StorageImpl(credentials);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        for (String inputFile : input.getInputFiles()) {
            byte[] chunk = storage.read(inputFile);
            outputStream.write(chunk);
        }

        byte[] mergedFile = outputStream.toByteArray();

        String outputfile = input.getOutputBucket() + "summarized.txt";

        storage.write(mergedFile, outputfile);

        return MergeOutput.builder()
                .outputFile(outputfile)
                .build();
    }
    
}
