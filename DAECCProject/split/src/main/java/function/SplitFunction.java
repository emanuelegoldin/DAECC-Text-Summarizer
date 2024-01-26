package function;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;

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

public class SplitFunction implements HttpFunction, RequestHandler<SplitInput, SplitOutput>{

    private static final Gson gson = new Gson();

    @Override
    public SplitOutput handleRequest(SplitInput input, Context context) {
        try {
            return exec(input);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {
        JsonObject body = gson.fromJson(request.getReader(), JsonObject.class);
        SplitInput input = gson.fromJson(body.toString(), SplitInput.class);
        SplitOutput output = exec(input);
        response.getWriter().write(gson.toJson(output));
    }
    

    private SplitOutput exec(SplitInput input) {
        Credentials credentials;
        try {
            credentials = Credentials.loadDefaultCredentials();
        } catch (Exception e) {
            throw new RuntimeException("Error while parsing credentials: " + e.getMessage());
        }

        Storage storage = new StorageImpl(credentials);
        byte[] inputPdf;
        try {
            inputPdf = storage.read(input.getInputBucket() + input.getInputFile());
        } catch (Exception e) {
            throw new RuntimeException("Failed to read file "+ input.getInputFile() +" from bucket "+ input.getInputBucket() +": " + e.getMessage());
        }
        try (// split pdf
        PDDocument document = PDDocument.load(inputPdf)) {
            Splitter splitting = new Splitter();
            List<PDDocument> Page = splitting.split(document);
            // upload each page as an individual document to cloud storage
            List<String> outputFileNames = new ArrayList<>();
            Iterator<PDDocument> iteration = Page.listIterator();
            int counter = 1;
            while (iteration.hasNext()) {
                String baseName = FilenameUtils.getBaseName(input.getInputFile());
                String outputFile = input.getOutputBucket() + "split/" + baseName + "-" + counter + ".pdf";
                PDDocument pd = iteration.next();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                pd.save(out);
                pd.close();
                storage.write(out.toByteArray(), outputFile);
                outputFileNames.add(outputFile);
                counter++;
            }
            document.close();
            return SplitOutput.builder()
                    .files(outputFileNames)
                    .filesCount(outputFileNames.size())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to split file(s): " + e.getMessage());
        }
    }
}
