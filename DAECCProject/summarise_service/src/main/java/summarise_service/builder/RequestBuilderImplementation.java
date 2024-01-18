package summarise_service.builder;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class RequestBuilderImplementation<Request> extends RequestBuilder<Request> {

    private Request request;

    public RequestBuilderImplementation(Request request) {
        this.request = request;
    }

    public RequestBuilderImplementation(Class<Request> requestClass) {
        try {
            this.request = requestClass.getConstructor().newInstance();
        }  catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Request build() {
        return request;
    }

    @Override
    protected <Value> RequestBuilder<Request> setField(String fieldName, Value value) {
        try {
            Field field = request.getClass().getField(fieldName);
            field.setAccessible(true);
            field.set(request, value);
        } catch (NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return this;
    }
    
}
