package requests.skelethon.requesters;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;
import requests.skelethon.Endpoint;
import requests.skelethon.HttpRequest;
import requests.skelethon.interfaces.CrudEndpointInterface;
import static io.restassured.RestAssured.given;

public class CrudRequester extends HttpRequest implements CrudEndpointInterface {
    public CrudRequester(RequestSpecification requestSpecification, Endpoint endpoint, ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
    }

    @Override
    public ValidatableResponse post(BaseModel model) {
        var body = model == null ? "" : model;
        return given()
                .spec(requestSpecification)
                .body(model)
                .post(endpoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    public ValidatableResponse get(long id) {
        return given()
                .spec(requestSpecification)
                .get(endpoint.getUrl())  // GET /endpoint/{id}
                .then()
                .spec(responseSpecification);
    }

    public ValidatableResponse put(long id, BaseModel model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .put(endpoint.getUrl())  // или как формируется URL с id
                .then()
                .spec(responseSpecification);
    }

    @Override
    public Object delete(long id) {
        return null;
    }

    public ValidatableResponse putExpectingError(long id, BaseModel model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .put(endpoint.getUrl())
                .then()
                .spec(responseSpecification);
    }

    public ValidatableResponse get() {
        return given()
                .spec(requestSpecification)
                .get(endpoint.getUrl())
                .then()
                .spec(responseSpecification);
    }

    public ValidatableResponse post(String rawBody) {
        return given()
                .spec(requestSpecification)
                .body(rawBody)
                .post(endpoint.getUrl())
                .then()
                .spec(responseSpecification);
    }

    public ValidatableResponse post() {
        return given()
                .spec(requestSpecification)
                .post(endpoint.getUrl())
                .then()
                .spec(responseSpecification);
    }
}
