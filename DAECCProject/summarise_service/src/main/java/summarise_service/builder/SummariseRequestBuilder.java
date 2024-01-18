package summarise_service.builder;

import summarise_service.SummarizerRequest;

public class SummariseRequestBuilder extends RequestBuilderImplementation<SummarizerRequest> {

    public SummariseRequestBuilder() {
        super(SummarizerRequest.class);
    }
    
    public SummariseRequestBuilder(SummarizerRequest request) {
        super(request);
    }

    public SummariseRequestBuilder setInputFile(String inputFile) {
        return (SummariseRequestBuilder) super.setField("inputFile", inputFile);
    }
}
