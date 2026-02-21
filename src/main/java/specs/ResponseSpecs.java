package specs;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;
import org.apache.http.HttpStatus;

public class ResponseSpecs {
    private ResponseSpecs() {
    }

    //Базовая спецификация
    private static ResponseSpecBuilder defaultResponseBuilder() {
        return new ResponseSpecBuilder();
    }

    //Спецификация статус-код 201
    public static ResponseSpecification entityWasCreated() {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_CREATED)
                .build();
    }

    //Спецификация статус-код 200
    public static ResponseSpecification ok() {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .build();
    }

    //Спецификация статус-код 400
    public static ResponseSpecification invalidDataProvided() {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .build();
    }

    //Спецификация статус-код 401
    public static ResponseSpecification invalidToken() {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_UNAUTHORIZED)
                .build();
    }

    //Спецификация статус-код 403
    public static ResponseSpecification invalidIdAccount() {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_FORBIDDEN)
                .build();
    }
}