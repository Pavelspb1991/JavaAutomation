package api.requests.skelethon.requesters;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import api.models.BaseModel;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.HttpRequest;
import api.requests.skelethon.interfaces.CrudEndpointInterface;

public class ValidatedCrudRequester<T extends BaseModel> extends HttpRequest implements CrudEndpointInterface {
    private CrudRequester crudRequester;

    public ValidatedCrudRequester(RequestSpecification requestSpecification, Endpoint endpoint, ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
        this.crudRequester = new CrudRequester(requestSpecification, endpoint, responseSpecification);
    }

    @Override
    public T post(BaseModel model) {
        return (T) crudRequester.post(model).extract().as(endpoint.getResponseModel());
    }

    @Override
    public T get(long id) {
        return (T) crudRequester.get(id)
                .extract()
                .as(endpoint.getResponseModel());
    }

    @Override
    public T put(long id, BaseModel model) {
        return (T) crudRequester.put(id, model)
                .extract()
                .as(endpoint.getResponseModel());
    }

    public String putExpectingErrorWithBody(long id, BaseModel model) {
        return crudRequester.put(id, model)
                .extract()
                .asString();
    }

    @Override
    public Object delete(long id) {
        return null;
    }

    public void putExpectingError(long id, BaseModel model) {
        crudRequester.put(id, model);
    }

    public T get() {
        return (T) crudRequester.get().extract().as(endpoint.getResponseModel());
    }
    public T post() {
        return (T) crudRequester.post().extract().as(endpoint.getResponseModel());
    }

    public void postExpectingError(BaseModel model) {
        crudRequester.post(model);
    }

    public String postExpectingErrorWithBody(BaseModel model) {
        return crudRequester.post(model).extract().asString();
    }

    public void postExpectingErrorWithRawBody(String rawBody) {
        crudRequester.post(rawBody);
    }


}