package ApiTestingNbank;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
public class TestDeposit {

    // Метод setup, объединил два метода (создать юзера + создать аккаунт),
    // так как если создать две аннотации beforeAll, то нет последовательности и тесты падают с 401
    @BeforeAll
    public static void setup() throws InterruptedException {
        // 1. Создаем пользователя
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
        .when().post()
                .then()
                .statusCode(201).log().body();

        Thread.sleep(2000);
        given()
                .baseUri("http://localhost:4111")
                .basePath("/api/v1/accounts")
                .contentType(ContentType.JSON)
                .auth().preemptive().basic("Test1234", "Test12345!")
        .when().post()
                .then()
                .log().body().statusCode(201);
    }

    // Метод, который возвращает стрим валидных значений депозита
    public static Stream<Number> validDepositValue() {
        return Stream.of(2500,
                0.01,
                5000,
                4999.99);
    }

    // Метод, который возвращает стрим невалидных значений депозита
    public static Stream<Object> invalidDepositValue() {
        return Stream.of(
                // Неправильные числа
                0,
                5000.01,
                -100,
                -0.01,
                "\"сто\"",
                "\"\"",
                "\" \"",
                "\"100abc\"",
                "\"abc100\"",
                "\"1.2.3\"",
                true,
                false,
                null
        );
    }

    //Проверка возможности депозита - валидные значения. Статус код 200.
    // Источник входных данных - @MethodSource("validDepositValue")
    @ParameterizedTest
    @MethodSource("validDepositValue")
    public void userCanDepositMoneyValidData(Number number) {
        given()
                .baseUri("http://localhost:4111")
                .basePath("/api/v1/accounts/deposit")
                .auth().preemptive().basic("Test1234", "Test12345!")
                .contentType(ContentType.JSON)
                .body("{\"id\": 1, \"balance\": " + number + "}")
        .when().post()
                .then()
                .log().ifError()
                .statusCode(200);
        // Проверка суммы депозита
        given()
                .baseUri("http://localhost:4111")
                .basePath("/api/v1/accounts/1/transactions")
                .auth().preemptive().basic("Test1234", "Test12345!")
                .contentType(ContentType.JSON)
                .when().get()
                .then()
                .statusCode(200)
                .body("amount", hasItem(number.floatValue()))
                .body("relatedAccountId", hasItem(1));


    }




    //Проверка возможности депозита - невалидные значения. Статус код 400.
    // Источник входных данных - @MethodSource("invalidDepositValue")
    @ParameterizedTest
    @MethodSource("invalidDepositValue")
    public void userCantDepositMoneyWithInvalidData(Object object) {
        given()
                .baseUri("http://localhost:4111")
                .basePath("/api/v1/accounts/deposit")
                .auth().preemptive().basic("Test1234", "Test12345!")
                .contentType(ContentType.JSON)
                .body("{\"id\": 1, \"balance\": " + object + "}")
        .when().post()
                .then()
                .log().ifError()
                .statusCode(not(200));

        // Проверка, что невалидных данных нет в массиве депозитов
        given()
                .baseUri("http://localhost:4111")
                .basePath("/api/v1/accounts/1/transactions")
                .auth().preemptive().basic("Test1234", "Test12345!")
                .contentType(ContentType.JSON)
                .when().get()
                .then()
                .statusCode(200)
                .statusCode(200)
                .body("amount", not(hasItem(object)));
    }

    //Проверка невозможности депозита без токена. Статус код 401
    @Test
    public void userCantDepositMoneyWithoutToken() {
        given()
                .baseUri("http://localhost:4111")
                .basePath("/api/v1/accounts/deposit")
                .contentType(ContentType.JSON)
                .body("{\"id\": 1, \"balance\":4988}") //захардкодил для уникальности
                .when().post()
                .then()
                .log().ifError()
                .statusCode(401);

        // Проверка, что невалидных данных нет в массиве депозитов
        given()
                .baseUri("http://localhost:4111")
                .basePath("/api/v1/accounts/1/transactions")
                .auth().preemptive().basic("Test1234", "Test12345!")
                .contentType(ContentType.JSON)
                .when().get()
                .then()
                .statusCode(200)
                .body("amount", not(hasItem(4988)));
    }

    //Проверка невозможности депозита с несуществующего аккаунта. Статус код 403
    @Test
    public void  userCannotDepositToNonExistentAccount() {
        given()
                .baseUri("http://localhost:4111")
                .basePath("/api/v1/accounts/deposit")
                .contentType(ContentType.JSON)
                .auth().preemptive().basic("Test1234", "Test12345!")
                .body("{\"id\": 999, \"balance\":4987}") //захардкодил для уникальности
        .when().post()
                .then()
                .log().ifError()
                .statusCode(403);

        // Проверка, что невалидных данных нет в массиве депозитов
        given()
                .baseUri("http://localhost:4111")
                .basePath("/api/v1/accounts/1/transactions")
                .auth().preemptive().basic("Test1234", "Test12345!")
                .contentType(ContentType.JSON)
                .when().get()
                .then()
                .statusCode(200)
                .body("amount", not(hasItem(4987)));
    }

}

