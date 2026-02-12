package ApiTestingNbank;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class TestTransferMoney {
    private static int SENDER_ACCOUNT_ID;
    private static int RECEIVER_ACCOUNT_ID;
    private static int ZERO_BALANCE_ACCOUNT_ID;

    // Метод setup, объединил два метода (создать юзера + создать два аккаунта для трансфера + депозит),
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

        //Создаем аккаунт 1, с которого будем совершать переводы
        int senderAccountId = given()
                .baseUri("http://localhost:4111")
                .basePath("/api/v1/accounts")
                .contentType(ContentType.JSON)
                .auth().preemptive().basic("Test1234", "Test12345!")
                .when().post()
                .then()
                .statusCode(201).log().body()
                .extract()
                .path("id");

        //Создаем аккаунт 2, на этот аккаунт будет совершать переводы
        int receiverAccountId = given()
                .baseUri("http://localhost:4111")
                .basePath("/api/v1/accounts")
                .contentType(ContentType.JSON)
                .auth().preemptive().basic("Test1234", "Test12345!")
                .when().post()
                .then()
                .statusCode(201).log().body()
                .extract()
                .path("id");
        Thread.sleep(2000);

        //Создаем аккаунт 3 с пустым балансом
        int zeroBalanceAccountId = given()
                .baseUri("http://localhost:4111")
                .basePath("/api/v1/accounts")
                .contentType(ContentType.JSON)
                .auth().preemptive().basic("Test1234", "Test12345!")
                .when().post()
                .then()
                .statusCode(201).log().body()
                .extract()
                .path("id");
        Thread.sleep(2000);

        SENDER_ACCOUNT_ID = senderAccountId;
        RECEIVER_ACCOUNT_ID = receiverAccountId;
        ZERO_BALANCE_ACCOUNT_ID = zeroBalanceAccountId;

        //Пополнение депозита для тестовых переводов
        for (int i = 0; i < 10; i++) {
            given()
                    .baseUri("http://localhost:4111")
                    .basePath("/api/v1/accounts/deposit")
                    .auth().preemptive().basic("Test1234", "Test12345!")
                    .contentType(ContentType.JSON)
                    .body("{\"id\": " + SENDER_ACCOUNT_ID + ", \"balance\": " + 5000 + "}")
                    .when().post()
                    .then();
        }
        Thread.sleep(2000);
    }

    // Метод для генерации валидных значений транзакции
    public  static  Stream<Number> validTransferAmount() {
        return  Stream.of(10000,
                0.01,
                5000,
                1,
                100,
                9999.99,
                0.1,
                5000.50,
                0.02,
                9999.99);
    }

    // Метод для генерации невалидных значений транзакции
    public  static  Stream<Object> invalidTransferAmount() {
        return  Stream.of(0,
                10000.01,
                0,
                -100,
                -0.01,
                0.001,
                10000.001,
                -5000,
                null,
                "",
                " ",
                1000000000);
    }

    // Проверка перевода с 1 аккаунта на 2 - валидные значения. Статус код 200. Источник входных данных -
    // @MethodSource("validTransferAmount")
    @ParameterizedTest
    @MethodSource("validTransferAmount")
    public void userCanTransferAmountValidData(Number amount) {
        given()
                .baseUri("http://localhost:4111")
                .basePath("/api/v1/accounts/transfer")
                .auth().preemptive().basic("Test1234", "Test12345!")
                .contentType(ContentType.JSON)
                .body("{\"senderAccountId\": " + SENDER_ACCOUNT_ID +
                        ", \"receiverAccountId\": " + RECEIVER_ACCOUNT_ID +
                        ", \"amount\": " + amount + "}")
                .when().post()
                .then()
                .log().all()
                .body("amount", equalTo(amount.floatValue()))
                .statusCode(200);

        //Проверка суммы перевода
        given()
                .baseUri("http://localhost:4111")
                .basePath("/api/v1/accounts/" + SENDER_ACCOUNT_ID + "/transactions")
                .auth().preemptive().basic("Test1234", "Test12345!")
                .contentType(ContentType.JSON)
                .when().get()
                .then()
                .statusCode(200)
                .body("type", hasItem("TRANSFER_OUT"))
                .body("amount", hasItem(amount.floatValue()));
    }

    // Проверка перевода с 1 аккаунта на 2 невалидные значения. Статус код 400
    // Источник входных данных - @MethodSource("invalidTransferAmount")
    @ParameterizedTest
    @MethodSource("invalidTransferAmount")
    public void userCantTransferAmountInvalidData(Object invalidAmount) {
        given()
                .baseUri("http://localhost:4111")
                .basePath("/api/v1/accounts/transfer")
                .auth().preemptive().basic("Test1234", "Test12345!")
                .contentType(ContentType.JSON)
                .body("{\"senderAccountId\": " + SENDER_ACCOUNT_ID +
                        ", \"receiverAccountId\": " + RECEIVER_ACCOUNT_ID +
                        ", \"amount\": " + invalidAmount + "}")
                .when().post()
                .then()
                .log().body()
                .statusCode(400);

        //Проверка того, что невалидных значений нет в истории переводов
        given()
                .baseUri("http://localhost:4111")
                .basePath("/api/v1/accounts/" + SENDER_ACCOUNT_ID + "/transactions")
                .auth().preemptive().basic("Test1234", "Test12345!")
                .contentType(ContentType.JSON)
                .when().get()
                .then()
                .statusCode(200)
                .body("amount", not(invalidAmount));
    }

    //Проверка невозможности перевода денег без авторизации - Статус код 401
    @Test
    public void userCantDepositMoneyWithoutToken() {
        given()
                .baseUri("http://localhost:4111")
                .basePath("/api/v1/accounts/transfer")
                .contentType(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body("{\"senderAccountId\": " + SENDER_ACCOUNT_ID +
                        ", \"receiverAccountId\": " + RECEIVER_ACCOUNT_ID +
                        ", \"amount\": " + 3199 + "}")
                .when().post()
                .then()
                .log().body()
                .statusCode(401);

        //Проверка того, что в истории переводов нет суммы, которая не должна передаваться без токена
        given()
                .baseUri("http://localhost:4111")
                .basePath("/api/v1/accounts/" + SENDER_ACCOUNT_ID + "/transactions")
                .auth().preemptive().basic("Test1234", "Test12345!")
                .contentType(ContentType.JSON)
                .when().get()
                .then()
                .statusCode(200)
                .body("amount", not(3199));
    }

    //Проверка невозможности перевода валидного значения на невалидный id. Статус код 403
    @Test
    public void userCantTransferAmountInvalidId() {
        given()
                .baseUri("http://localhost:4111")
                .basePath("/api/v1/accounts/transfer")
                .auth().preemptive().basic("Test1234", "Test12345!")
                .contentType(ContentType.JSON)
                .body("{\"senderAccountId\": " + 100 +
                        ", \"receiverAccountId\": " + RECEIVER_ACCOUNT_ID +
                        ", \"amount\": " + 3198 + "}")
                .when().post()
                .then()
                .log().body()
                .statusCode(403);

        //Проверка того, что в истории переводов нет суммы, которая не должна передаваться без токена
        given()
                .baseUri("http://localhost:4111")
                .basePath("/api/v1/accounts/" + SENDER_ACCOUNT_ID + "/transactions")
                .auth().preemptive().basic("Test1234", "Test12345!")
                .contentType(ContentType.JSON)
                .when().get()
                .then()
                .statusCode(200)
                .body("amount", not(3198));
    }

    //Проверка невозможности перевода денег при 0 балансе у отправителя. Статус код 400
    @Test
    public void userCantTransferWithZeroBalanceAmount() {
        given()
                .baseUri("http://localhost:4111")
                .basePath("/api/v1/accounts/transfer")
                .auth().preemptive().basic("Test1234", "Test12345!")
                .contentType(ContentType.JSON)
                .body("{\"senderAccountId\": " + ZERO_BALANCE_ACCOUNT_ID +
                        ", \"receiverAccountId\": " + RECEIVER_ACCOUNT_ID +
                        ", \"amount\": " + 0.01 + "}")
                .when().post()
                .then()
                .statusCode(400);

        //Проверка того, что в истории переводов нет суммы, которая не должна передаваться без токена
        given()
                .baseUri("http://localhost:4111")
                .basePath("/api/v1/accounts/" + ZERO_BALANCE_ACCOUNT_ID + "/transactions")
                .auth().preemptive().basic("Test1234", "Test12345!")
                .contentType(ContentType.JSON)
                .when().get()
                .then()
                .statusCode(200)
                .body("amount", not(0.01));
    }

}



