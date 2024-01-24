package summarise_service;

import summarise_service.builder.Request;
import summarise_service.builder.SummariseRequestBuilder;

public class SummarizerRequest implements Request<SummariseRequestBuilder> {
    public String inputFile;

    public SummarizerRequest() {}

    @Override
    public SummariseRequestBuilder getBuilder() {
        return new SummariseRequestBuilder(this);
    }
}
