package ApiTestingNbank;

import api.dao.AccountDao;
import api.dao.UserDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.models.*;
import api.requests.steps.DataBaseSteps;
import common.annotations.UserSession;
import common.extensions.UserSessionExtension;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import api.requests.steps.AccountSteps;
import api.specs.ResponseSpecs;
import java.util.List;

@ExtendWith(UserSessionExtension.class)
public class TestDeposit extends BaseTest {

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
                .orElseThrow(() -> new RuntimeException("Аккаунт не найден: " + id));
    }

    @ParameterizedTest
    @UserSession
    @ValueSource(doubles = {2500, 0.01, 5000, 4999.99})
    public void userCanDepositMoneyValidData(double amount) {
        CreateAccountResponse account = accountSteps().createAccount();

        List<Account> accounts = accountSteps().getAccounts();
        Account beforeAccount = findAccountById(accounts, account.getId());
        Number previousBalance = beforeAccount.getBalance();

        DepositMoneyResponse response = accountSteps().deposit(account.getId(), amount);

        softly.assertThat(response.getBalance().doubleValue())
                .isEqualTo(previousBalance.doubleValue() + amount);

        accounts = accountSteps().getAccounts();
        Account afterAccount = findAccountById(accounts, account.getId());
        softly.assertThat(afterAccount.getBalance().doubleValue())
                .isEqualTo(previousBalance.doubleValue() + amount);

        boolean transactionExists = afterAccount.getTransactions().stream()
                .anyMatch(tx -> tx.getAmount().doubleValue() == amount);
        softly.assertThat(transactionExists).isTrue();

        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(account.getAccountNumber());
        DaoAndModelAssertions.assertThat(response, accountDao).match();
    }

    @ParameterizedTest
    @UserSession
    @ValueSource(doubles = {0, -100, -0.01, 5000.01})
    public void userCantDepositMoneyWithInvalidData(double invalidAmount) {
        CreateAccountResponse account = accountSteps().createAccount();

        List<Account> accounts = accountSteps().getAccounts();
        Account beforeAccount = findAccountById(accounts, account.getId());
        Number previousBalance = beforeAccount.getBalance();
        int previousTxCount = beforeAccount.getTransactions().size();

        accountSteps().depositExpectingError(account.getId(), invalidAmount,
                ResponseSpecs.invalidDataProvided());

        accounts = accountSteps().getAccounts();
        Account afterAccount = findAccountById(accounts, account.getId());

        softly.assertThat(afterAccount.getBalance().doubleValue())
                .isEqualTo(previousBalance.doubleValue());
        softly.assertThat(afterAccount.getTransactions().size())
                .isEqualTo(previousTxCount);
    }

    @Test
    @UserSession
    public void userCantDepositMoneyWithoutToken() {
        CreateAccountResponse account = accountSteps().createAccount();

        List<Account> accounts = accountSteps().getAccounts();
        Account beforeAccount = findAccountById(accounts, account.getId());
        Number previousBalance = beforeAccount.getBalance();
        int previousTxCount = beforeAccount.getTransactions().size();

        accountSteps().depositWithoutAuth(account.getId(), AccountSteps.generateRandomTransferAmount());

        accounts = accountSteps().getAccounts();
        Account afterAccount = findAccountById(accounts, account.getId());

        softly.assertThat(afterAccount.getBalance().doubleValue())
                .isEqualTo(previousBalance.doubleValue());
        softly.assertThat(afterAccount.getTransactions().size())
                .isEqualTo(previousTxCount);
    }

    @Test
    @UserSession
    public void userCannotDepositToNonExistentAccount() {
        CreateAccountResponse account = accountSteps().createAccount();

        List<Account> accounts = accountSteps().getAccounts();
        Account beforeAccount = findAccountById(accounts, account.getId());
        Number previousBalance = beforeAccount.getBalance();

        Long nonExistentId = AccountSteps.generateNonExistentAccountId();
        accountSteps().depositExpectingError(nonExistentId, AccountSteps.generateRandomTransferAmount(),
                ResponseSpecs.invalidSenderAccount());

        accounts = accountSteps().getAccounts();
        Account afterAccount = findAccountById(accounts, account.getId());

        softly.assertThat(afterAccount.getBalance().doubleValue())
                .isEqualTo(previousBalance.doubleValue());
    }
}
