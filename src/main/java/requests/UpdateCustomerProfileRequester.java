package requests;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;
import static io.restassured.RestAssured.given;

public class UpdateCustomerProfileRequester extends Request{

    public UpdateCustomerProfileRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    //Метод для генерации отправки put-запроса на ручку - /api/v1/profile
    @Override
    public ValidatableResponse put(BaseModel model) {
        return  given()
                .spec(requestSpecification)
                .body(model)
                .put("/api/v1/customer/profile")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}