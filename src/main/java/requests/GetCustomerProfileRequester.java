package requests;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import static io.restassured.RestAssured.given;

public class GetCustomerProfileRequester extends Request{

    public GetCustomerProfileRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    //Метод для генерации отправки get-запроса на ручку - /api/v1/profile
    @Override
    public ValidatableResponse get() {
        return  given()
                .spec(requestSpecification)
                .get("/api/v1/customer/profile")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}