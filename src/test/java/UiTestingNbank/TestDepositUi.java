package UiTestingNbank;

import api.models.Account;
import api.models.CustomerData;
import common.annotations.Browsers;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ui.pages.BankAlert;
import ui.pages.UserDashboard;

public class TestDepositUi extends BaseUiTest {

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

        CustomerData profile = SessionStorage.getSteps().getProfile();
        Account account = profile.getAccounts().stream()
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