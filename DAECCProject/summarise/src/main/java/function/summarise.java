package function;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import summarise_service.FileType;
import summarise_service.Provider;
import summarise_service.SummarizeService;
import summarise_service.SummarizerFactory;
import summarise_service.SummarizerResponse;

import shared.Credentials;
import storage.Storage;
import storage.StorageImpl;

public class summarise implements RequestHandler<SummariseInput, SummariseOutput>, HttpFunction {
    private static final Gson gson = new Gson();

    @Override
    public SummariseOutput handleRequest(SummariseInput event, Context context) {
        try {
            return exec(event);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
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
            response.getWriter().write(gson.toJson(output));
        } catch (Exception e) {
            response.setStatusCode(500);
            response.getWriter().write("Error executing function: " + e.getMessage());
        }

    }

    private SummariseOutput exec(SummariseInput input) throws Exception {

        Credentials credentials;
        try {
            credentials = Credentials.loadDefaultCredentials();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load credentials: " + e.getMessage());
        }

        Storage storage = new StorageImpl(credentials);
        try {
            storage.read(input.getInputBucket() + input.getInputFile());
        } catch (Exception e) {
            throw new RuntimeException("Failed to read file "+ input.getInputFile() +" from bucket "+ input.getInputBucket() +": " + e.getMessage());
        }

        Provider provider = StringToProvider(input.provider);
        FileType fileType = StringToFileType(input.inputType);
        SummarizeService service = CreateSummrizeService(provider, fileType, input.inputFile);

        byte[] file = storage.read(input.getInputBucket() + input.getInputFile());

        // Load the PDF document
        PDDocument document = PDDocument.load(new ByteArrayInputStream(file));

        // Create a PDFTextStripper
        PDFTextStripper pdfStripper = new PDFTextStripper();

        // Extract the text from the PDF
        String text = pdfStripper.getText(document);

        // Close the document
        document.close();

        
        SummarizerResponse summary = service.summarize(text);
        
        String baseName = FilenameUtils.getBaseName(input.getInputFile());
        String outputFile = input.getOutputBucket() + "summary/" + baseName + ".txt";

        storage.write(summary.summary.getBytes(), outputFile);
        return SummariseOutput.builder().outputfile(outputFile).build();
    }


    private SummarizeService CreateSummrizeService(Provider provider, FileType fileType, String inputUrl) {
        if (provider != null) {
            return SummarizerFactory.Create(provider);
        } else if (fileType != null) {
            return SummarizerFactory.Create(fileType);
        } else if (inputUrl != null) {
            return SummarizerFactory.Create(inputUrl);
        } else {
            return null;
        }
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

    private FileType StringToFileType(String fileType) {
        switch (fileType) {
            case "AWS":
                return FileType.PLAIN;
            case "GCP":
            default:
                return FileType.PDF;
        }
    }
}
