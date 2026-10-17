package UiTestingNbank;

import api.models.Account;
import api.requests.steps.AccountSteps;
import common.annotations.Browsers;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import common.utils.RepeatUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ui.pages.UserDashboard;
import java.util.List;

import static common.utils.Constants.MAX_DEPOSIT_AMOUNT;

public class TestTransferMoneyUi extends BaseUiTest {

    @ParameterizedTest
    @UserSession
    @Browsers({"chrome"})
    @ValueSource(doubles = {10000, 0.01, 5000, 9999.99, 5000.50, 0.02})
    public void userCanTransferAmountValidData(double amount) {
        UserDashboard dashboard = new UserDashboard().open();
        String senderAccount = dashboard.createAccountAndGetNumber();
        String receiverAccount = dashboard.createAccountAndGetNumber();

        AccountSteps steps = new AccountSteps(
                SessionStorage.getUser().getUsername(),
                SessionStorage.getUser().getPassword()
        );
        List<Account> accounts = steps.getAccounts();
        Account sender = accounts.stream()
                .filter(a -> a.getAccountNumber().contains(senderAccount))
                .findFirst().orElseThrow();
        Account receiver = accounts.stream()
                .filter(a -> a.getAccountNumber().contains(receiverAccount))
                .findFirst().orElseThrow();

        RepeatUtils.repeat(10, () -> {
            steps.deposit(sender.getId(), MAX_DEPOSIT_AMOUNT);
        });

        dashboard
                .goToTransfer()
                .selectSenderAccount(senderAccount)
                .enterRecipient("Receiver")
                .enterRecipientAccount(receiverAccount)
                .enterAmount(amount)
                .confirm()
                .sendTransferAndCheckSuccessAlert();

        accounts = steps.getAccounts();
        Account receiverAfter = accounts.stream()
                .filter(a -> a.getId().equals(receiver.getId()))
                .findFirst().orElseThrow();
        assert receiverAfter.getBalance().doubleValue() >= amount;
    }

    @ParameterizedTest
    @UserSession
    @Browsers({"chrome"})
    @ValueSource(doubles = {0, 10000.01, -100, -0.01})
    public void userCantTransferAmountInvalidData(double invalidAmount) {
        UserDashboard dashboard = new UserDashboard().open();
        String senderAccount = dashboard.createAccountAndGetNumber();
        String receiverAccount = dashboard.createAccountAndGetNumber();

        AccountSteps steps = new AccountSteps(
                SessionStorage.getUser().getUsername(),
                SessionStorage.getUser().getPassword()
        );
        List<Account> accounts = steps.getAccounts();
        Account sender = accounts.stream()
                .filter(a -> a.getAccountNumber().contains(senderAccount))
                .findFirst().orElseThrow();

        RepeatUtils.repeat(10, () -> {
            steps.deposit(sender.getId(), MAX_DEPOSIT_AMOUNT);
        });

        dashboard
                .goToTransfer()
                .selectSenderAccount(senderAccount)
                .enterRecipient("Receiver")
                .enterRecipientAccount(receiverAccount)
                .enterAmount(invalidAmount)
                .confirm()
                .sendTransferAndCheckNegativeAlert();
    }
}
