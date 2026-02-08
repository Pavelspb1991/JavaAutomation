package ApiTestingNbank;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.equalTo;
public class TestChangeNamee {

    // метод createUser для создания пользователя, выполняется 1 раз перед всеми тестами
    @BeforeAll
    public static void createUser() throws InterruptedException {
        given()
                .baseUri("http://localhost:4111")
                .basePath("/api/v1/admin/users")
                .contentType(ContentType.JSON)
                .auth().preemptive().basic("admin", "admin")
                .body("""
                {
                    "username": "Test1234",
                    "password": "Test12345!",
                    "role": "USER"
                }
                """)
        .when()
                .post()
                .then()
                .log().ifError();
        Thread.sleep(1000);
    }

    // Метод, который возвращает стрим валидных значений
    static Stream<String> validNames() {
        return Stream.of("A B",
                "Ab Cd",
                "John Doe",
                "Abcdefghijaaaa Klmnopqrstaaaaaaaa");
    }

    // Метод, который возвращает стрим невалидных значений
    static Stream<String> invalidNames() {
        return Stream.of("A",
                "AbCd",
                "John1 Doe",
                "John Doe1",
                "John1 Doe Jack",
                "Привет",
                "");
    }

    // Параметризованный тест, проверка возможности изменения имени пользователя - валидные данные
    //Источник входных данных - @MethodSource("validNames")
    @ParameterizedTest
    @MethodSource("validNames")
    public void userCanChangeNameCheckValidData(String newName) {
        given()
                .baseUri("http://localhost:4111")
                .basePath("/api/v1/customer/profile")
                .auth().preemptive().basic("Test1234", "Test12345!")
                .contentType(ContentType.JSON)
                .body("{\"name\": \"" + newName + "\"}")
        .when().put()
        .then()
                .log().ifError()
                .statusCode(200).assertThat().body("customer.name", equalTo(newName));
    }

    //Параметризованный тест на проверку невозможности изменения имени пользователя при невалидных данных.
    //Источник входных данных - @MethodSource("invalidNames")
    @ParameterizedTest
    @MethodSource("invalidNames")
    public void userCantChangeNameWithInvalidData(String invalidName) {
        given()
                .baseUri("http://localhost:4111")
                .basePath("/api/v1/customer/profile")
                .auth().preemptive().basic("Test1234", "Test12345!")
                .contentType(ContentType.JSON)
                .body("{\"name\": \"" + invalidName + "\"}")
        .when().put()
        .then()
                .log().ifError()
                .statusCode(400);
    }

    //Тест на проверку невозможности изменения имени пользователя без авторизации
    @Test
    public void userCantChangeNameWithoutToken() {
        given()
                .baseUri("http://localhost:4111")
                .basePath("/api/v1/customer/profile")
                .contentType(ContentType.JSON)
                .body("{\"name\": \"Some string\"}")
        .when().put()
                .then()
                .log().ifError()
                .statusCode(401);
    }
}
