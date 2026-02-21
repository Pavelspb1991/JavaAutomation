package specs;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import java.util.Base64;
import java.util.List;

public class RequestSpecs {
    private RequestSpecs() {}

    //Базовая спецификация
    private static RequestSpecBuilder defaultRequestBuilder() {
        return new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilters(List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()))
                .setBaseUri("http://localhost:4111/");
    }

    //Спецификация для юзера без токена
    public static RequestSpecification unauthSpec() {
        return defaultRequestBuilder().build();
    }

    //Спецификация для админа
    public static RequestSpecification adminSpec() {  // переименуй для ясности
        return defaultRequestBuilder()
                .addHeader("Authorization", "Basic YWRtaW46YWRtaW4=")
                .build();
    }

    //Спецификация для юзера с токеном (кодируем в base64)
    public static RequestSpecification userSpec(String username, String password) {
        String credentials = username + ":" + password;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());
        return defaultRequestBuilder().addHeader("Authorization", "Basic " + encoded).build();
    }
}