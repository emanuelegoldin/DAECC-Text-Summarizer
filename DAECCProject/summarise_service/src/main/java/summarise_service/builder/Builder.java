package summarise_service.builder;

public abstract class Builder<T> {
    public abstract T build();
    protected abstract <V> Builder<T> setField(String field, V value);
}
