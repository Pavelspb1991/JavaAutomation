package ApiTestingNbank;
import models.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import requests.steps.AccountSteps;
import requests.steps.UserSteps;
import specs.ResponseSpecs;

import java.util.List;
import java.util.stream.Stream;

public class TestTransferMoney extends BaseTest{
    private UserSteps userSteps;
    private AccountSteps accountSteps;

    private static CreateAccountResponse senderAccount;
    private static CreateAccountResponse receiverAccount;
    private static CreateAccountResponse zeroBalanceAccount;

    @BeforeAll
    public static void setup() {
        String username = createdUserRequest.getUsername();
        String password = createdUserRequest.getPassword();
        AccountSteps tempSteps = new AccountSteps(username, password);
        senderAccount = tempSteps.createAccount();
        receiverAccount = tempSteps.createAccount();
        zeroBalanceAccount = tempSteps.createAccount();

        for (int i = 0; i < 10; i++) {
            tempSteps.deposit(senderAccount.getId(), 5000);
        }
    }

    @BeforeEach
    public void initSteps() {
        userSteps = new UserSteps(
                createdUserRequest.getUsername(),
                createdUserRequest.getPassword(),
                createdUserResponse.getId()
        );
        accountSteps = new AccountSteps(
                createdUserRequest.getUsername(),
                createdUserRequest.getPassword()
        );
    }

    @ParameterizedTest
    @ValueSource(doubles = {
            10000, 0.01, 5000, 1, 100, 9999.99, 0.1, 5000.50, 0.02, 9999.99
    })
    public void userCanTransferAmountValidData(double amount) {
        CustomerData beforeData = userSteps.getProfile();
        Account senderBefore = findAccountById(beforeData.getAccounts(), senderAccount.getId());
        Account receiverBefore = findAccountById(beforeData.getAccounts(), receiverAccount.getId());

        Number senderBalanceBefore = senderBefore.getBalance();
        Number receiverBalanceBefore = receiverBefore.getBalance();

        TransferMoneyResponse response = accountSteps.transfer(
                senderAccount.getId(),
                receiverAccount.getId(),
                amount
        );

        softly.assertThat(response.getSenderAccountId())
                .isEqualTo(senderAccount.getId());
        softly.assertThat(response.getReceiverAccountId())
                .isEqualTo(receiverAccount.getId());
        softly.assertThat(response.getAmount())
                .isEqualTo(amount);
        softly.assertThat(response.getMessage())
                .isEqualTo(SuccessMessages.TRANSFER_SUCCESSFUL);

        CustomerData afterData = userSteps.getProfile();
        Account senderAfter = findAccountById(afterData.getAccounts(), senderAccount.getId());
        Account receiverAfter = findAccountById(afterData.getAccounts(), receiverAccount.getId());

        softly.assertThat(senderAfter.getBalance().doubleValue())
                .isEqualTo(senderBalanceBefore.doubleValue() - amount);
        softly.assertThat(receiverAfter.getBalance().doubleValue())
                .isEqualTo(receiverBalanceBefore.doubleValue() + amount);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, 10000.01, -100, -0.01, 0.001, 10000.001, -5000, 1000000000})
    public void userCantTransferAmountInvalidData(double invalidAmount) {
        CustomerData beforeData = userSteps.getProfile();
        Account receiverBefore = findAccountById(beforeData.getAccounts(), receiverAccount.getId());
        Number receiverBalanceBefore = receiverBefore.getBalance();

        accountSteps.transferExpectingError(
                senderAccount.getId(),
                receiverAccount.getId(),
                invalidAmount,
                ResponseSpecs.invalidDataProvided()
        );

        CustomerData afterData = userSteps.getProfile();
        Account receiverAfter = findAccountById(afterData.getAccounts(), receiverAccount.getId());
        softly.assertThat(receiverAfter.getBalance().doubleValue())
                .as("Receiver balance should not change")
                .isEqualTo(receiverBalanceBefore.doubleValue());
    }

    static Stream<Object> invalidNonNumericValues() {
        return Stream.of(
                null, "", " ", "abc", "100abc", true, false
        );
    }

    @ParameterizedTest
    @MethodSource("invalidNonNumericValues")
    public void userCantTransferAmountInvalidNonNumericData(Object invalidValue) {
        CustomerData beforeData = userSteps.getProfile();
        Account receiverBefore = findAccountById(beforeData.getAccounts(), receiverAccount.getId());
        Number receiverBalanceBefore = receiverBefore.getBalance();

        String jsonBody = String.format(
                "{\"senderAccountId\": %d,\"receiverAccountId\": %d, \"amount\": %s}",
                senderAccount.getId(),
                receiverAccount.getId(),
                formatJsonValue(invalidValue)
        );
        accountSteps.transferExpectingErrorWithRawBody(jsonBody, ResponseSpecs.invalidDataProvided());
        CustomerData afterData = userSteps.getProfile();
        Account receiverAfter = findAccountById(afterData.getAccounts(), receiverAccount.getId());
        softly.assertThat(receiverAfter.getBalance().doubleValue())
                .as("Receiver balance should not change")
                .isEqualTo(receiverBalanceBefore.doubleValue());
    }

    @Test
    public void userCantTransferMoneyWithoutToken() {
        CustomerData beforeData = userSteps.getProfile();
        Account receiverBefore = findAccountById(beforeData.getAccounts(), receiverAccount.getId());
        Number receiverBalanceBefore = receiverBefore.getBalance();

        accountSteps.transferWithoutAuth(senderAccount.getId(), receiverAccount.getId(), 1000.0);

        CustomerData afterData = userSteps.getProfile();
        Account receiverAfter = findAccountById(afterData.getAccounts(), receiverAccount.getId());
        softly.assertThat(receiverAfter.getBalance().doubleValue())
                .as("Receiver balance should not change without auth")
                .isEqualTo(receiverBalanceBefore.doubleValue());
    }

    @Test
    public void userCantTransferToInvalidAccount() {
        Long invalidAccountId = receiverAccount.getId() + 100;
        CustomerData beforeData = userSteps.getProfile();
        Account receiverBefore = findAccountById(beforeData.getAccounts(), receiverAccount.getId());
        Number receiverBalanceBefore = receiverBefore.getBalance();

        accountSteps.transferExpectingError(
                senderAccount.getId(),
                invalidAccountId,
                1000.0,
                ResponseSpecs.invalidIdAccount()
        );

        CustomerData afterData = userSteps.getProfile();
        Account receiverAfter = findAccountById(afterData.getAccounts(), receiverAccount.getId());
        softly.assertThat(receiverAfter.getBalance().doubleValue())
                .isEqualTo(receiverBalanceBefore.doubleValue());
    }

    @Test
    public void userCantTransferFromInvalidAccount() {
        Long invalidAccountId = senderAccount.getId() + 100;
        CustomerData beforeData = userSteps.getProfile();
        Account receiverBefore = findAccountById(beforeData.getAccounts(), receiverAccount.getId());
        Number receiverBalanceBefore = receiverBefore.getBalance();

        accountSteps.transferExpectingError(
                invalidAccountId,
                receiverAccount.getId(),
                1000.0,
                ResponseSpecs.invalidIdAccount()
        );

        CustomerData afterData = userSteps.getProfile();
        Account receiverAfter = findAccountById(afterData.getAccounts(), receiverAccount.getId());
        softly.assertThat(receiverAfter.getBalance().doubleValue())
                .isEqualTo(receiverBalanceBefore.doubleValue());
    }

    @Test
    public void userCantTransferWithZeroBalance() {
        CustomerData beforeData = userSteps.getProfile();
        Account receiverBefore = findAccountById(beforeData.getAccounts(), receiverAccount.getId());
        Account senderBefore = findAccountById(beforeData.getAccounts(), zeroBalanceAccount.getId());

        Number receiverBalanceBefore = receiverBefore.getBalance();
        Number senderBalanceBefore = senderBefore.getBalance();

        accountSteps.transferExpectingError(
                zeroBalanceAccount.getId(),
                receiverAccount.getId(),
                0.1,
                ResponseSpecs.invalidDataProvided()
        );

        CustomerData afterData = userSteps.getProfile();
        Account receiverAfter = findAccountById(afterData.getAccounts(), receiverAccount.getId());
        Account senderAfter = findAccountById(afterData.getAccounts(), zeroBalanceAccount.getId());

        softly.assertThat(receiverAfter.getBalance().doubleValue())
                .isEqualTo(receiverBalanceBefore.doubleValue());
        softly.assertThat(senderAfter.getBalance().doubleValue())
                .isEqualTo(senderBalanceBefore.doubleValue());
    }

    private Account findAccountById(List<Account> accounts, Long id) {
        return accounts.stream()
                .filter(acc -> acc.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Account not found: " + id));
    }

    private String formatJsonValue(Object value) {
        return switch (value) {
            case null -> "null";
            case String s -> "\"" + s + "\"";
            case Boolean b -> b.toString();
            case Number n -> n.toString();
            default -> "\"" + value.toString() + "\"";
        };
    }
}