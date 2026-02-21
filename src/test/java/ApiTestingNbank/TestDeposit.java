package ApiTestingNbank;

import models.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import requests.CreateNewAccountRequester;
import requests.DepositMoneyRequester;
import requests.GetCustomerProfileRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;
import java.util.stream.Stream;

public class TestDeposit extends BaseTest{
    public static CreateAccountResponse createAccountResponse;

    //Создаем один аккаунт для тестов (id=1)
    @BeforeAll
    public static void  createAccount() throws InterruptedException {
        String username = createdUserRequest.getUsername();
        String password = createdUserRequest.getPassword();
        createAccountResponse = new CreateNewAccountRequester(RequestSpecs.userSpec(username,password),
                ResponseSpecs.entityWasCreated()).post().extract().as(CreateAccountResponse.class);
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

    //Вспомогательный метод для негативных проверок, используется в userCantDepositMoneyWithInvalidData
    private String formatJsonValue(Object value) {
        return switch (value) {
            case null -> "null";
            case String string ->
                    "\"" + value + "\"";
            case Boolean b -> value.toString();
            case Number number -> value.toString();
            default ->
                    "\"" + value.toString() + "\"";
        };
    }

    //Проверка возможности депозита - валидные значения. Статус код 200.
    // Источник входных данных - @MethodSource("validDepositValue")
    @ParameterizedTest
    @MethodSource("validDepositValue")
    public void userCanDepositMoneyValidData(Number number) {
        //Извлекаем логин и пароль из запроса на создание юзера
        String username = createdUserRequest.getUsername();
        String password = createdUserRequest.getPassword();

        //Создаем тело запроса для метода post
        DepositMoneyRequest request = DepositMoneyRequest.builder()
                .id(createAccountResponse.getId()).balance(number).build();

        //Отправляем get запрос для проверки
        CustomerData beforeData = new GetCustomerProfileRequester(RequestSpecs.userSpec(username,password),
                ResponseSpecs.ok()).get().extract().as(CustomerData.class);

        //Извлекаем нужный для теста аккаунт
        Account beforeAccount = beforeData.getAccounts().stream()
                .filter(acc -> acc.getId() == createAccountResponse.getId())
                .findFirst()
                .orElseThrow();

        //Извлекаем баланс до пополнения депозита
        Number previousBalance = beforeAccount.getBalance();

        //Отправляем post-запрос с депозитом и проверяем на 200 код
        DepositMoneyResponse response = new DepositMoneyRequester(RequestSpecs.userSpec(username,password),
                ResponseSpecs.ok()).post(request).extract().as(DepositMoneyResponse.class);

        //Отправляем get запрос для проверки после пополнения депозита
        CustomerData afterData = new GetCustomerProfileRequester(RequestSpecs.userSpec(username, password),
                ResponseSpecs.ok()).get().extract().as(CustomerData.class);

        //Извлекаем нужный для теста аккаунт после депозита
        Account afterAccount = afterData.getAccounts().stream()
                .filter(acc -> acc.getId() == createAccountResponse.getId())
                .findFirst()
                .orElseThrow();

        //Проверка, что баланс изменился на сумму, которую передали из стрима validDepositValue()
        softly.assertThat(afterAccount.getBalance().doubleValue())
                .isEqualTo(previousBalance.doubleValue() + number.doubleValue());
        //Проверка, что сумма есть в транзакциях, если есть, то возвращаем ок через isTrue
        softly.assertThat(afterAccount.getTransactions().stream()
                        .anyMatch(tx -> tx.getAmount().doubleValue() == number.doubleValue()))
                .isTrue();
    }

    //Проверка возможности депозита - невалидные значения. Статус код 400.
    // Источник входных данных - @MethodSource("invalidDepositValue")
    @ParameterizedTest
    @MethodSource("invalidDepositValue")
    public void userCantDepositMoneyWithInvalidData(Object invalidValue) {
        //Извлекаем логин и пароль из запроса на создание юзера
        String username = createdUserRequest.getUsername();
        String password = createdUserRequest.getPassword();

        //Отправляем get запрос для проверки
        CustomerData beforeData = new GetCustomerProfileRequester(RequestSpecs.userSpec(username,password),
                ResponseSpecs.ok()).get().extract().as(CustomerData.class);

        //Формируем сырой json через String.format, тк pojo-класс не может этого сделать
        String jsonBody = String.format(
                "{\"id\": %d, \"balance\": %s}",
                createAccountResponse.getId(),
                formatJsonValue(invalidValue));

        //Извлекаем нужный для теста аккаунт
        Account beforeAccount = beforeData.getAccounts().stream()
                .filter(acc -> acc.getId() == createAccountResponse.getId())
                .findFirst()
                .orElseThrow();
        //Извлекаем баланс и размер транзакций до пополнения депозита из beforeAccount
        Number previousBalance = beforeAccount.getBalance();
        int previousTxCount = beforeAccount.getTransactions().size();

        //Отправляем post-запрос с депозитом и проверяем на 400 код, но без записи в переменную
        new DepositMoneyRequester(RequestSpecs.userSpec(username,password),
                ResponseSpecs.invalidDataProvided()).post(jsonBody);

        //Отправляем get запрос для проверки после пополнения депозита
        CustomerData afterData = new GetCustomerProfileRequester(
                RequestSpecs.userSpec(username, password), ResponseSpecs.ok()).get()
                .extract().as(CustomerData.class);

        //Извлекаем нужный для теста аккаунт
        Account afterAccount = afterData.getAccounts().stream()
                .filter(acc -> acc.getId() == createAccountResponse.getId())
                .findFirst()
                .orElseThrow();

        // Проверка, что баланс не изменился (сравниваем значения из двух get-запросов)
        softly.assertThat(afterAccount.getBalance().doubleValue())
                .isEqualTo(previousBalance.doubleValue());
        //Проверка, что размер массива transactions не изменился
        softly.assertThat(afterAccount.getTransactions().size())
                .isEqualTo(previousTxCount);

    }

    //Проверка невозможности депозита без токена. Статус код 401
    @Test
    public void userCantDepositMoneyWithoutToken() {
        //Извлекаем логин и пароль из запроса на создание юзера
        String username = createdUserRequest.getUsername();
        String password = createdUserRequest.getPassword();

        //Отправляем get запрос для проверки
        CustomerData beforeData = new GetCustomerProfileRequester(RequestSpecs.userSpec(username,password),
                ResponseSpecs.ok()).get().extract().as(CustomerData.class);

        //извлекаем нужный нам аккаунт
        Account beforeAccount = beforeData.getAccounts().stream()
                .filter(acc -> acc.getId() == createAccountResponse.getId())
                .findFirst()
                .orElseThrow();

        //Извлекаем баланс и размер транзакций до пополнения депозита
        Number previousBalance = beforeAccount.getBalance();
        int previousTxCount = beforeAccount.getTransactions().size();

        //Создаем тело запроса для метода post
        DepositMoneyRequest request = DepositMoneyRequest.builder()
                .id(createAccountResponse.getId()).balance(100).build();

        //Отправляем post-запрос с депозитом
        new DepositMoneyRequester(RequestSpecs.unauthSpec(),
                ResponseSpecs.invalidToken()).post(request);

        //Отправляем get запрос для проверки после пополнения депозита
        CustomerData afterData = new GetCustomerProfileRequester(
                RequestSpecs.userSpec(username, password), ResponseSpecs.ok()).get()
                .extract().as(CustomerData.class);

        //извлекаем нужный нам аккаунт
        Account afterAccount = afterData.getAccounts().stream()
                .filter(acc -> acc.getId() == createAccountResponse.getId())
                .findFirst()
                .orElseThrow();

        // Проверка, что баланс не изменился (сравниваем значения из двух get-запросов)
        softly.assertThat(afterAccount.getBalance().doubleValue())
                .isEqualTo(previousBalance.doubleValue());
        //Проверка, что размер массива transactions не изменился
        softly.assertThat(afterAccount.getTransactions().size())
                .isEqualTo(previousTxCount);
    }

    //Проверка невозможности депозита с несуществующего аккаунта. Статус код 403
    @Test
    public void  userCannotDepositToNonExistentAccount() {
        //Извлекаем логин и пароль из запроса на создание юзера
        String username = createdUserRequest.getUsername();
        String password = createdUserRequest.getPassword();

        //Отправляем get запрос для проверки
        CustomerData beforeData = new GetCustomerProfileRequester(RequestSpecs.userSpec(username,password),
                ResponseSpecs.ok()).get().extract().as(CustomerData.class);

        //Извлечение целевого аккаунта по id
        Account beforeAccount = beforeData.getAccounts().stream()
                .filter(acc -> acc.getId() == createAccountResponse.getId())
                .findFirst()
                .orElseThrow();

        //Извлекаем баланс и размер транзакций до пополнения депозита
        Number previousBalance = beforeAccount.getBalance();
        int previousTxCount = beforeAccount.getTransactions().size();

        //Создаем тело для post-запроса
        DepositMoneyRequest request = DepositMoneyRequest.builder()
                .id(createAccountResponse.getId() + 100).balance(100).build();

        //Отправляем post-запрос
        new DepositMoneyRequester(RequestSpecs.userSpec(username,password),
                ResponseSpecs.invalidIdAccount()).post(request);

        //Отправляем get запрос для проверки
        CustomerData afterData = new GetCustomerProfileRequester(
                RequestSpecs.userSpec(username, password), ResponseSpecs.ok()).get()
                .extract().as(CustomerData.class);

        //Извлекаем id нужного нам аккаунта после депозита
        Account afterAccount = afterData.getAccounts().stream()
                .filter(acc -> acc.getId() == createAccountResponse.getId())
                .findFirst()
                .orElseThrow();

        // Проверка, что баланс не изменился (сравниваем значения из двух get-запросов)
        softly.assertThat(afterAccount.getBalance().doubleValue())
                .isEqualTo(previousBalance.doubleValue());
        //Проверка, что размер массива transactions не изменился
        softly.assertThat(afterAccount.getTransactions().size())
                .isEqualTo(previousTxCount);
    }
}

