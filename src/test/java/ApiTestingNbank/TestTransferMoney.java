package ApiTestingNbank;
import models.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import requests.CreateNewAccountRequester;
import requests.DepositMoneyRequester;
import requests.GetCustomerProfileRequester;
import requests.TransferMoneyRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;
import java.util.stream.Stream;

public class TestTransferMoney extends BaseTest{
    public static CreateAccountResponse createAccountResponse;
    public static CreateAccountResponse createAccountResponseReceiver;
    public static CreateAccountResponse createAccountResponseZeroBalance;

    //Создаем три аккаунта для юзера и добавляем сумму на баланс с depositToAccount()
    @BeforeAll
    public static void setup() {
        String username = createdUserRequest.getUsername();
        String password = createdUserRequest.getPassword();
        createAccountResponse = new CreateNewAccountRequester(RequestSpecs.userSpec(username,password),
                ResponseSpecs.entityWasCreated()).post().extract().as(CreateAccountResponse.class);
        createAccountResponseReceiver = new CreateNewAccountRequester(RequestSpecs.userSpec(username,password),
                ResponseSpecs.entityWasCreated()).post().extract().as(CreateAccountResponse.class);
        createAccountResponseZeroBalance = new CreateNewAccountRequester(RequestSpecs.userSpec(username,password),
                ResponseSpecs.entityWasCreated()).post().extract().as(CreateAccountResponse.class);

        for(int i=0; i < 10;i++) {
            depositToAccount(createAccountResponse.getId(), 5000);
        }
    }

    //Вспомогательный метод, вызывается в beforeAll. Добавляет необходимую сумму баланса для перевода
    private static void depositToAccount(int accountId, Number amount) {
        String username = createdUserRequest.getUsername();
        String password = createdUserRequest.getPassword();

        DepositMoneyRequest request = DepositMoneyRequest.builder().id(accountId).balance(amount).build();
        new DepositMoneyRequester(RequestSpecs.userSpec(username, password), ResponseSpecs.ok()).post(request);
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

    // Проверка перевода с 1 аккаунта на 2 - валидные значения. Статус код 200. Источник входных данных -
    // @MethodSource("validTransferAmount")
    @ParameterizedTest
    @MethodSource("validTransferAmount")
    public void userCanTransferAmountValidData(Number amount) {
        //Извлекаем логин и пароль из запроса на создание юзера
        String username = createdUserRequest.getUsername();
        String password = createdUserRequest.getPassword();

        //Отправляем get запрос для проверки
        CustomerData beforeData = new GetCustomerProfileRequester(RequestSpecs.userSpec(username,password),
                ResponseSpecs.ok()).get().extract().as(CustomerData.class);

        //Извлекаем нужный для теста аккаунт получателя до перевода
        Account receiverAccount = beforeData.getAccounts().stream()
                .filter(acc -> acc.getId() == createAccountResponseReceiver.getId())
                .findFirst()
                .orElseThrow();

        //Извлекаем баланс аккаунта 2 до перевода
        Number receiverAccountBalance = receiverAccount.getBalance();

        //Извлекаем нужный для теста аккаунт отправителя после перевода
        Account senderBefore = beforeData.getAccounts().stream()
                .filter(acc -> acc.getId() == createAccountResponse.getId())
                .findFirst()
                .orElseThrow();

        //Извлекаем баланс аккаунта 1 (отправителя) до перевода
        Number senderBalanceBefore = senderBefore.getBalance();

        //Создаем тело для post-запроса
        TransferMoneyRequest request = TransferMoneyRequest.builder().senderAccountId(createAccountResponse.getId())
                .receiverAccountId(createAccountResponseReceiver.getId()).amount(amount)
                .build();

        //Отправляем post-запрос
        TransferMoneyResponse response = new TransferMoneyRequester(RequestSpecs.userSpec(username,password),
                ResponseSpecs.ok()).post(request).extract().as(TransferMoneyResponse.class);

        //Отправляем get запрос для проверки
        CustomerData afterData = new GetCustomerProfileRequester(RequestSpecs.userSpec(username,password),
                ResponseSpecs.ok()).get().extract().as(CustomerData.class);

        //Извлекаем нужный для теста аккаунт получателя после перевода
        Account receiverAccountAfter = afterData.getAccounts().stream()
                .filter(acc -> acc.getId() == createAccountResponseReceiver.getId())
                .findFirst()
                .orElseThrow();

        //Извлекаем нужный для теста аккаунт отправителя после перевода
        Account senderAfter = afterData.getAccounts().stream()
                .filter(acc -> acc.getId() == createAccountResponse.getId())
                .findFirst()
                .orElseThrow();

        //Проверки тела ответа
        softly.assertThat(response.getSenderAccountId()).isEqualTo(request.getSenderAccountId());
        softly.assertThat(response.getReceiverAccountId()).isEqualTo(request.getReceiverAccountId());
        softly.assertThat(response.getAmount()).isEqualTo(request.getAmount().doubleValue());
        softly.assertThat(response.getMessage()).isEqualTo(SuccessMessages.TRANSFER_SUCCESSFUL);

        //Проверка, что баланс у аккаунта получателя изменился на сумму перевода
        softly.assertThat(receiverAccountAfter.getBalance().doubleValue())
                .isEqualTo(receiverAccountBalance.doubleValue() + amount.doubleValue());

        //Проверка, что баланс у аккаунта отправителя изменился на сумму перевода
        softly.assertThat(senderAfter.getBalance().doubleValue())
                .isEqualTo(senderBalanceBefore.doubleValue() - amount.doubleValue());
    }

    // Проверка перевода с 1 аккаунта на 2 невалидные значения. Статус код 400
    // Источник входных данных - @MethodSource("invalidTransferAmount")
    @ParameterizedTest
    @MethodSource("invalidTransferAmount")
    public void userCantTransferAmountInvalidData(Object invalidAmount) {
        //Извлекаем логин и пароль из запроса на создание юзера
        String username = createdUserRequest.getUsername();
        String password = createdUserRequest.getPassword();

        //Отправляем get запрос для проверки
        CustomerData beforeData = new GetCustomerProfileRequester(RequestSpecs.userSpec(username,password),
                ResponseSpecs.ok()).get().extract().as(CustomerData.class);

        //Извлекаем нужный для теста аккаунт до перевода
        Account receiverAccount = beforeData.getAccounts().stream()
                .filter(acc -> acc.getId() == createAccountResponseReceiver.getId())
                .findFirst()
                .orElseThrow();

        //Извлекаем баланс до пополнения депозита
        Number receiverAccountBalance = receiverAccount.getBalance();

        //Формируем сырой json через String.format, тк pojo-класс не может этого сделать
        String jsonBody = String.format(
                "{\"senderAccountId\": %d,\"receiverAccountId\": %d, \"amount\": %s}",
                createAccountResponse.getId(),
                createAccountResponseReceiver.getId(),
                formatJsonValue(invalidAmount));

        //Отправляем post-запрос
        new TransferMoneyRequester(RequestSpecs.userSpec(username,password), ResponseSpecs.invalidDataProvided())
                .post(jsonBody);

        //Отправляем get-запрос после перевода
        CustomerData afterData = new GetCustomerProfileRequester(RequestSpecs.userSpec(username, password),
                ResponseSpecs.ok()).get().extract().as(CustomerData.class);

        //Извлекаем нужный для теста после перевода
        Account receiverAccountAfter = afterData.getAccounts().stream()
                .filter(acc -> acc.getId() == createAccountResponseReceiver.getId())
                .findFirst()
                .orElseThrow();

        //Проверка, что баланс у аккаунта 2 не изменился
        softly.assertThat(receiverAccountAfter.getBalance().doubleValue())
                .isEqualTo(receiverAccountBalance.doubleValue());
    }

    //Проверка невозможности перевода денег без авторизации - Статус код 401
    @Test
    public void userCantDepositMoneyWithoutToken() {
        //Извлекаем логин и пароль из запроса на создание юзера
        String username = createdUserRequest.getUsername();
        String password = createdUserRequest.getPassword();

        //Отправляем get запрос для проверки
        CustomerData beforeData = new GetCustomerProfileRequester(RequestSpecs.userSpec(username,password),
                ResponseSpecs.ok()).get().extract().as(CustomerData.class);

        //Извлекаем нужный для теста аккаунт до перевода
        Account receiverAccount = beforeData.getAccounts().stream()
                .filter(acc -> acc.getId() == createAccountResponseReceiver.getId())
                .findFirst()
                .orElseThrow();

        //Извлекаем баланс до пополнения депозита
        Number receiverAccountBalance = receiverAccount.getBalance();

        //Формируем тело для post-запроса
        TransferMoneyRequest request = TransferMoneyRequest.builder()
                .senderAccountId(createAccountResponse.getId())
                .receiverAccountId(createAccountResponseReceiver.getId())
                .amount(1000).build();

        //Отправляем post-запрос
        new TransferMoneyRequester(RequestSpecs.unauthSpec(),ResponseSpecs.invalidToken()).post(request);

        //Отправляем get-запрос после перевода
        CustomerData afterData = new GetCustomerProfileRequester(RequestSpecs.userSpec(username, password),
                ResponseSpecs.ok()).get().extract().as(CustomerData.class);

        //Извлекаем нужный для теста после перевода
        Account receiverAccountAfter = afterData.getAccounts().stream()
                .filter(acc -> acc.getId() == createAccountResponseReceiver.getId())
                .findFirst()
                .orElseThrow();


        softly.assertThat(receiverAccountAfter.getBalance().doubleValue())
                .isEqualTo(receiverAccountBalance.doubleValue());
    }

    //Проверка невозможности перевода валидного значения на невалидный id. Статус код 403
    @Test
    public void userCantTransferAmountInvalidId() { //Извлекаем логин и пароль из запроса на создание юзера
        String username = createdUserRequest.getUsername();
        String password = createdUserRequest.getPassword();

        //Отправляем get запрос для проверки
        CustomerData beforeData = new GetCustomerProfileRequester(RequestSpecs.userSpec(username, password),
                ResponseSpecs.ok()).get().extract().as(CustomerData.class);

        //Извлекаем нужный для теста аккаунт до перевода
        Account receiverAccount = beforeData.getAccounts().stream()
                .filter(acc -> acc.getId() == createAccountResponseReceiver.getId())
                .findFirst()
                .orElseThrow();

        //Извлекаем баланс до пополнения депозита
        Number receiverAccountBalance = receiverAccount.getBalance();

        //Формируем тело для post-запроса
        TransferMoneyRequest request = TransferMoneyRequest.builder()
                .senderAccountId(createAccountResponse.getId() + 100)
                .receiverAccountId(createAccountResponseReceiver.getId())
                .amount(1000).build();

        //Отправляем post-запрос
        new TransferMoneyRequester(RequestSpecs.userSpec(username, password), ResponseSpecs.invalidIdAccount())
                .post(request);

        //Отправляем get-запрос после перевода
        CustomerData afterData = new GetCustomerProfileRequester(RequestSpecs.userSpec(username, password),
                ResponseSpecs.ok()).get().extract().as(CustomerData.class);

        //Извлекаем нужный для теста после перевода
        Account receiverAccountAfter = afterData.getAccounts().stream()
                .filter(acc -> acc.getId() == createAccountResponseReceiver.getId())
                .findFirst()
                .orElseThrow();

        softly.assertThat(receiverAccountAfter.getBalance().doubleValue())
                .isEqualTo(receiverAccountBalance.doubleValue());
    }

    //Проверка невозможности перевода денег при 0 балансе у отправителя. Статус код 400
    @Test
    public void userCantTransferWithZeroBalanceAmount() {
        //Извлекаем логин и пароль из запроса на создание юзера
        String username = createdUserRequest.getUsername();
        String password = createdUserRequest.getPassword();

        //Отправляем get запрос для проверки
        CustomerData beforeData = new GetCustomerProfileRequester(RequestSpecs.userSpec(username,password),
                ResponseSpecs.ok()).get().extract().as(CustomerData.class);

        //Извлекаем нужный для теста аккаунт получателя до перевода
        Account receiverAccount = beforeData.getAccounts().stream()
                .filter(acc -> acc.getId() == createAccountResponseReceiver.getId())
                .findFirst()
                .orElseThrow();

        //Извлекаем баланс аккаунта 2 до перевода
        Number receiverAccountBalance = receiverAccount.getBalance();


        //Создаем тело для post-запроса
        TransferMoneyRequest request = TransferMoneyRequest.builder()
                .senderAccountId(createAccountResponseZeroBalance.getId())
                .receiverAccountId(createAccountResponseReceiver.getId()).amount(0.1)
                .build();

        //Отправляем post-запрос
        new TransferMoneyRequester(RequestSpecs.userSpec(username,password),
                ResponseSpecs.invalidDataProvided()).post(request);

        //Отправляем get запрос для проверки
        CustomerData afterData = new GetCustomerProfileRequester(RequestSpecs.userSpec(username,password),
                ResponseSpecs.ok()).get().extract().as(CustomerData.class);

        //Извлекаем нужный для теста аккаунт получателя после перевода
        Account receiverAccountAfter = afterData.getAccounts().stream()
                .filter(acc -> acc.getId() == createAccountResponseReceiver.getId())
                .findFirst()
                .orElseThrow();

        //Извлекаем нужный для теста аккаунт отправителя после перевода
        Account senderAfter = afterData.getAccounts().stream()
                .filter(acc -> acc.getId() == createAccountResponseZeroBalance.getId())
                .findFirst()
                .orElseThrow();

        //Проверка, что баланс получателя не изменился
        softly.assertThat(receiverAccountAfter.getBalance().doubleValue())
                .isEqualTo(receiverAccountBalance.doubleValue());

        //Проверка, что баланс у аккаунта отправителя не изменился на сумму перевода и равен 0
        softly.assertThat(senderAfter.getBalance().doubleValue())
                .isEqualTo(0.0);
    }
}