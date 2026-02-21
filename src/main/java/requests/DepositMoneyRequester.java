package requests;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;
import static io.restassured.RestAssured.given;

public class DepositMoneyRequester extends Request{
    public DepositMoneyRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    //метод для отправки post запроса с телом на ручку /api/v1/accounts/deposit
    @Override
    public ValidatableResponse post(BaseModel model) {
        return  given()
                .spec(requestSpecification)
                .body(model)
                .post("/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
    //перегрузка метода post, чтобы отправлять различные негативные значения в body
    public ValidatableResponse post(String jsonBody) {
        return given()
                .spec(requestSpecification)
                .body(jsonBody)
                .post("/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}