package ApiTestingNbank;

import api.models.*;
import common.annotations.UserSession;
import common.extensions.UserSessionExtension;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import api.requests.steps.AccountSteps;
import api.specs.ResponseSpecs;
import java.util.List;
import java.util.stream.Stream;

@ExtendWith(UserSessionExtension.class)
public class TestTransferMoney extends BaseTest {

    private AccountSteps accountSteps() {
        return new AccountSteps(
                SessionStorage.getUser().getUsername(),
                SessionStorage.getUser().getPassword()
        );
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

    @ParameterizedTest
    @UserSession
    @ValueSource(doubles = {10000, 0.01, 5000, 1, 100, 9999.99, 0.1, 5000.50, 0.02})
    public void userCanTransferAmountValidData(double amount) {
        AccountSteps steps = accountSteps();
        CreateAccountResponse senderAccount = steps.createAccount();
        CreateAccountResponse receiverAccount = steps.createAccount();

        for (int i = 0; i < 10; i++) {
            steps.deposit(senderAccount.getId(), 5000);
        }

        CustomerData beforeData = SessionStorage.getSteps().getProfile();
        Account senderBefore = findAccountById(beforeData.getAccounts(), senderAccount.getId());
        Account receiverBefore = findAccountById(beforeData.getAccounts(), receiverAccount.getId());

        Number senderBalanceBefore = senderBefore.getBalance();
        Number receiverBalanceBefore = receiverBefore.getBalance();

        TransferMoneyResponse response = steps.transfer(
                senderAccount.getId(), receiverAccount.getId(), amount);

        softly.assertThat(response.getSenderAccountId()).isEqualTo(senderAccount.getId());
        softly.assertThat(response.getReceiverAccountId()).isEqualTo(receiverAccount.getId());
        softly.assertThat(response.getAmount()).isEqualTo(amount);
        softly.assertThat(response.getMessage()).isEqualTo(SuccessMessages.TRANSFER_SUCCESSFUL);

        CustomerData afterData = SessionStorage.getSteps().getProfile();
        Account senderAfter = findAccountById(afterData.getAccounts(), senderAccount.getId());
        Account receiverAfter = findAccountById(afterData.getAccounts(), receiverAccount.getId());

        softly.assertThat(senderAfter.getBalance().doubleValue())
                .isEqualTo(senderBalanceBefore.doubleValue() - amount);
        softly.assertThat(receiverAfter.getBalance().doubleValue())
                .isEqualTo(receiverBalanceBefore.doubleValue() + amount);
    }

    @ParameterizedTest
    @UserSession
    @ValueSource(doubles = {0, 10000.01, -100, -0.01, 0.001, 10000.001, -5000, 1000000000})
    public void userCantTransferAmountInvalidData(double invalidAmount) {
        AccountSteps steps = accountSteps();
        CreateAccountResponse senderAccount = steps.createAccount();
        CreateAccountResponse receiverAccount = steps.createAccount();

        CustomerData beforeData = SessionStorage.getSteps().getProfile();
        Account receiverBefore = findAccountById(beforeData.getAccounts(), receiverAccount.getId());
        Number receiverBalanceBefore = receiverBefore.getBalance();

        steps.transferExpectingError(senderAccount.getId(), receiverAccount.getId(),
                invalidAmount, ResponseSpecs.invalidDataProvided());

        CustomerData afterData = SessionStorage.getSteps().getProfile();
        Account receiverAfter = findAccountById(afterData.getAccounts(), receiverAccount.getId());
        softly.assertThat(receiverAfter.getBalance().doubleValue())
                .isEqualTo(receiverBalanceBefore.doubleValue());
    }

    static Stream<Object> invalidNonNumericValues() {
        return Stream.of(null, "", " ", "abc", "100abc", true, false);
    }

    @ParameterizedTest
    @UserSession
    @MethodSource("invalidNonNumericValues")
    public void userCantTransferAmountInvalidNonNumericData(Object invalidValue) {
        AccountSteps steps = accountSteps();
        CreateAccountResponse senderAccount = steps.createAccount();
        CreateAccountResponse receiverAccount = steps.createAccount();

        CustomerData beforeData = SessionStorage.getSteps().getProfile();
        Account receiverBefore = findAccountById(beforeData.getAccounts(), receiverAccount.getId());
        Number receiverBalanceBefore = receiverBefore.getBalance();

        String jsonBody = String.format(
                "{\"senderAccountId\": %d,\"receiverAccountId\": %d, \"amount\": %s}",
                senderAccount.getId(), receiverAccount.getId(), formatJsonValue(invalidValue));
        steps.transferExpectingErrorWithRawBody(jsonBody, ResponseSpecs.invalidDataProvided());

        CustomerData afterData = SessionStorage.getSteps().getProfile();
        Account receiverAfter = findAccountById(afterData.getAccounts(), receiverAccount.getId());
        softly.assertThat(receiverAfter.getBalance().doubleValue())
                .isEqualTo(receiverBalanceBefore.doubleValue());
    }

    @Test
    @UserSession
    public void userCantTransferMoneyWithoutToken() {
        AccountSteps steps = accountSteps();
        CreateAccountResponse senderAccount = steps.createAccount();
        CreateAccountResponse receiverAccount = steps.createAccount();

        CustomerData beforeData = SessionStorage.getSteps().getProfile();
        Account receiverBefore = findAccountById(beforeData.getAccounts(), receiverAccount.getId());
        Number receiverBalanceBefore = receiverBefore.getBalance();

        steps.transferWithoutAuth(senderAccount.getId(), receiverAccount.getId(),
                AccountSteps.generateRandomTransferAmount());

        CustomerData afterData = SessionStorage.getSteps().getProfile();
        Account receiverAfter = findAccountById(afterData.getAccounts(), receiverAccount.getId());
        softly.assertThat(receiverAfter.getBalance().doubleValue())
                .isEqualTo(receiverBalanceBefore.doubleValue());
    }

    @Test
    @UserSession
    public void userCantTransferToInvalidAccount() {
        AccountSteps steps = accountSteps();
        CreateAccountResponse senderAccount = steps.createAccount();
        CreateAccountResponse receiverAccount = steps.createAccount();

        Long invalidAccountId = AccountSteps.generateNonExistentAccountId();
        CustomerData beforeData = SessionStorage.getSteps().getProfile();
        Account receiverBefore = findAccountById(beforeData.getAccounts(), receiverAccount.getId());
        Number receiverBalanceBefore = receiverBefore.getBalance();

        steps.transferExpectingError(senderAccount.getId(), invalidAccountId,
                AccountSteps.generateRandomTransferAmount(), ResponseSpecs.invalidIdAccount());

        CustomerData afterData = SessionStorage.getSteps().getProfile();
        Account receiverAfter = findAccountById(afterData.getAccounts(), receiverAccount.getId());
        softly.assertThat(receiverAfter.getBalance().doubleValue())
                .isEqualTo(receiverBalanceBefore.doubleValue());
    }

    @Test
    @UserSession
    public void userCantTransferFromInvalidAccount() {
        AccountSteps steps = accountSteps();
        CreateAccountResponse receiverAccount = steps.createAccount();

        Long invalidAccountId = AccountSteps.generateNonExistentAccountId();
        CustomerData beforeData = SessionStorage.getSteps().getProfile();
        Account receiverBefore = findAccountById(beforeData.getAccounts(), receiverAccount.getId());
        Number receiverBalanceBefore = receiverBefore.getBalance();

        steps.transferExpectingError(invalidAccountId, receiverAccount.getId(),
                AccountSteps.generateRandomTransferAmount(), ResponseSpecs.invalidIdAccount());

        CustomerData afterData = SessionStorage.getSteps().getProfile();
        Account receiverAfter = findAccountById(afterData.getAccounts(), receiverAccount.getId());
        softly.assertThat(receiverAfter.getBalance().doubleValue())
                .isEqualTo(receiverBalanceBefore.doubleValue());
    }

    @Test
    @UserSession
    public void userCantTransferWithZeroBalance() {
        AccountSteps steps = accountSteps();
        CreateAccountResponse zeroBalanceAccount = steps.createAccount();
        CreateAccountResponse receiverAccount = steps.createAccount();

        CustomerData beforeData = SessionStorage.getSteps().getProfile();
        Account receiverBefore = findAccountById(beforeData.getAccounts(), receiverAccount.getId());
        Account senderBefore = findAccountById(beforeData.getAccounts(), zeroBalanceAccount.getId());

        Number receiverBalanceBefore = receiverBefore.getBalance();
        Number senderBalanceBefore = senderBefore.getBalance();

        steps.transferExpectingError(zeroBalanceAccount.getId(), receiverAccount.getId(),
                AccountSteps.generateRandomTransferAmount(), ResponseSpecs.invalidDataProvided());

        CustomerData afterData = SessionStorage.getSteps().getProfile();
        Account receiverAfter = findAccountById(afterData.getAccounts(), receiverAccount.getId());
        Account senderAfter = findAccountById(afterData.getAccounts(), zeroBalanceAccount.getId());

        softly.assertThat(receiverAfter.getBalance().doubleValue())
                .isEqualTo(receiverBalanceBefore.doubleValue());
        softly.assertThat(senderAfter.getBalance().doubleValue())
                .isEqualTo(senderBalanceBefore.doubleValue());
    }
}