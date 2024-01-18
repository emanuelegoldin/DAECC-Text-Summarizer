package summarise_service.builder;

public abstract class RequestBuilder<RequestType> {
    public abstract RequestType build();
    protected abstract <Value> RequestBuilder<RequestType> setField(String field, Value value);
}
