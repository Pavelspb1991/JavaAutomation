package ApiTestingNbank;

import models.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import requests.steps.AccountSteps;
import requests.steps.UserSteps;
import specs.ResponseSpecs;
import java.util.List;

public class TestDeposit extends BaseTest {

    private UserSteps userSteps;
    private AccountSteps accountSteps;
    private static CreateAccountResponse createdAccount;

    @BeforeAll
    public static void createAccountForTests() {
        String username = createdUserRequest.getUsername();
        String password = createdUserRequest.getPassword();
        AccountSteps tempSteps = new AccountSteps(username, password);
        createdAccount = tempSteps.createAccount();
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
    @ValueSource(doubles = {2500, 0.01, 5000, 4999.99})
    public void userCanDepositMoneyValidData(double amount) {
        CustomerData beforeData = userSteps.getProfile();
        Account beforeAccount = findAccountById(beforeData.getAccounts(), createdAccount.getId());
        Number previousBalance = beforeAccount.getBalance();

        DepositMoneyResponse response = accountSteps.deposit(createdAccount.getId(), amount);

        softly.assertThat(response.getBalance().doubleValue())
                .isEqualTo(previousBalance.doubleValue() + amount);

        CustomerData afterData = userSteps.getProfile();
        Account afterAccount = findAccountById(afterData.getAccounts(), createdAccount.getId());
        softly.assertThat(afterAccount.getBalance().doubleValue())
                .isEqualTo(previousBalance.doubleValue() + amount);

        boolean transactionExists = afterAccount.getTransactions().stream()
                .anyMatch(tx -> tx.getAmount().doubleValue() == amount);
        softly.assertThat(transactionExists)
                .isTrue();
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, -100, -0.01, 5000.01})
    public void userCantDepositMoneyWithInvalidData(double invalidAmount) {
        // GIVEN
        CustomerData beforeData = userSteps.getProfile();
        Account beforeAccount = findAccountById(beforeData.getAccounts(), createdAccount.getId());
        Number previousBalance = beforeAccount.getBalance();
        int previousTxCount = beforeAccount.getTransactions().size();

        accountSteps.depositExpectingError(createdAccount.getId(), invalidAmount,
                ResponseSpecs.invalidDataProvided());

        CustomerData afterData = userSteps.getProfile();
        Account afterAccount = findAccountById(afterData.getAccounts(), createdAccount.getId());

        softly.assertThat(afterAccount.getBalance().doubleValue())
                .isEqualTo(previousBalance.doubleValue());
        softly.assertThat(afterAccount.getTransactions().size())
                .isEqualTo(previousTxCount);
    }

    @Test
    public void userCantDepositMoneyWithoutToken() {
        CustomerData beforeData = userSteps.getProfile();
        Account beforeAccount = findAccountById(beforeData.getAccounts(), createdAccount.getId());
        Number previousBalance = beforeAccount.getBalance();
        int previousTxCount = beforeAccount.getTransactions().size();

        accountSteps.depositWithoutAuth(createdAccount.getId(), 100);

        CustomerData afterData = userSteps.getProfile();
        Account afterAccount = findAccountById(afterData.getAccounts(), createdAccount.getId());

        softly.assertThat(afterAccount.getBalance().doubleValue())
                .isEqualTo(previousBalance.doubleValue());
        softly.assertThat(afterAccount.getTransactions().size())
                .isEqualTo(previousTxCount);
    }

    @Test
    public void userCannotDepositToNonExistentAccount() {
        CustomerData beforeData = userSteps.getProfile();
        Account beforeAccount = findAccountById(beforeData.getAccounts(), createdAccount.getId());
        Number previousBalance = beforeAccount.getBalance();

        Long nonExistentId = createdAccount.getId() + 100;
        accountSteps.depositExpectingError(nonExistentId, 100,
                ResponseSpecs.invalidIdAccount());

        CustomerData afterData = userSteps.getProfile();
        Account afterAccount = findAccountById(afterData.getAccounts(), createdAccount.getId());

        softly.assertThat(afterAccount.getBalance().doubleValue())
                .isEqualTo(previousBalance.doubleValue());
    }

    private Account findAccountById(List<Account> accounts, Long id) {
        return accounts.stream()
                .filter(acc -> acc.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Акааунт не найден: " + id));
    }
}

