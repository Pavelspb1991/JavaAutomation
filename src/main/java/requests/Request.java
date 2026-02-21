package requests;
import models.BaseModel;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

public abstract class Request<T extends BaseModel> {

    protected RequestSpecification requestSpecification;
    protected ResponseSpecification responseSpecification;

    public Request(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        this.responseSpecification = responseSpecification;
        this.requestSpecification = requestSpecification;
    }

    public ValidatableResponse post(T model) {
            throw new UnsupportedOperationException(
                    "POST не поддерживается в " + this.getClass().getSimpleName()
            );
    }

    public ValidatableResponse get() {
        throw new UnsupportedOperationException(
                "GET не поддерживается в " + this.getClass().getSimpleName()
        );
    }
    public ValidatableResponse put(T model) {
        throw new UnsupportedOperationException(
                "PUT не поддерживается в " + this.getClass().getSimpleName()
        );
    }
}