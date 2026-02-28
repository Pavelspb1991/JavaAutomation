package requests;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;
import static io.restassured.RestAssured.given;

public class CreateNewAccountRequester extends Request{

    public CreateNewAccountRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    //Метод для генерации отправки  post-запроса на ручку - /api/v1/accounts
    @Override
    public ValidatableResponse post(BaseModel model) {
        return  given()
                .spec(requestSpecification)
                .body(model)
                .post("/api/v1/accounts")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    public ValidatableResponse post() {
        return given()
                .spec(requestSpecification)
                .post("/api/v1/accounts")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}