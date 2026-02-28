package requests;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;
import static io.restassured.RestAssured.given;

public class CreateNewUserRequester extends Request{

    public CreateNewUserRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    //Метод для генерации отправки  post-запроса на ручку - /api/v1/users
    @Override
    public ValidatableResponse post(BaseModel model) {
        return  given()
                .spec(requestSpecification)
                .body(model)
                .post("/api/v1/admin/users")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}