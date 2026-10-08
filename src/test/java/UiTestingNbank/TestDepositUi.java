package UiTestingNbank;

import api.models.Account;
import common.annotations.Browsers;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ui.pages.BankAlert;
import ui.pages.UserDashboard;
import api.requests.steps.AccountSteps;
import java.util.List;

public class TestDepositUi extends BaseUiTest {

    @Disabled("Frontend bug: POST /accounts/deposit sends {id, balance} instead of {accountId, amount}")
    @ParameterizedTest
    @UserSession
    @Browsers({"chrome"})
    @ValueSource(doubles = {2500, 0.01, 5000, 4999.99})
    public void userCanDepositMoneyValidData(double amount) {
        UserDashboard dashboard = new UserDashboard();
        String accountNumber = dashboard.createAccountAndGetNumber();
        dashboard
                .goToDeposit()
                .selectAccount(accountNumber)
                .enterAmount(amount)
                .clickDeposit()
                .checkAlertMessageAndAccept(BankAlert.DEPOSIT_SUCCESSFUL.getMessage());

        List<Account> accounts = new AccountSteps(
                SessionStorage.getUser().getUsername(),
                SessionStorage.getUser().getPassword()
        ).getAccounts();
        Account account = accounts.stream()
                .filter(a -> a.getAccountNumber().contains(accountNumber))
                .findFirst()
                .orElseThrow();
        assert account.getBalance().doubleValue() == amount;
    }

    @ParameterizedTest
    @UserSession
    @Browsers({"chrome"})
    @ValueSource(doubles = {0, -100, -0.01, 5000.01})
    public void userCantDepositMoneyWithInvalidData(double invalidAmount) {
        UserDashboard dashboard = new UserDashboard();
        String accountNumber = dashboard.createAccountAndGetNumber();
        dashboard
                .goToDeposit()
                .selectAccount(accountNumber)
                .enterAmount(invalidAmount)
                .clickDepositAndCheckNegativeAlert();
    }
}
