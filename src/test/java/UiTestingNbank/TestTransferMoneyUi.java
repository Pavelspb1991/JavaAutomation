package UiTestingNbank;

import api.models.Account;
import api.models.CustomerData;
import api.requests.steps.AccountSteps;
import api.requests.steps.UserSteps;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ui.pages.UserDashboard;

public class TestTransferMoneyUi extends BaseUiTest {

    @ParameterizedTest
    @ValueSource(doubles = {10000, 0.01, 5000, 9999.99, 5000.50, 0.02})
    public void userCanTransferAmountValidData(double amount) {
        authAsUser(createdUserRequest);
        UserDashboard dashboard = new UserDashboard().open();
        String senderAccount = dashboard.createAccountAndGetNumber();
        String receiverAccount = dashboard.createAccountAndGetNumber();

        CustomerData profile = new UserSteps(
                createdUserRequest.getUsername(),
                createdUserRequest.getPassword(),
                createdUserResponse.getId()
        ).getProfile();
        Account sender = profile.getAccounts().stream()
                .filter(a -> a.getAccountNumber().contains(senderAccount))
                .findFirst().orElseThrow();
        Account receiver = profile.getAccounts().stream()
                .filter(a -> a.getAccountNumber().contains(receiverAccount))
                .findFirst().orElseThrow();
        AccountSteps steps = new AccountSteps(
                createdUserRequest.getUsername(),
                createdUserRequest.getPassword()
        );
        for (int i = 0; i < 10; i++) {
            steps.deposit(sender.getId(), 5000);
        }
        dashboard
                .goToTransfer()
                .selectSenderAccount(senderAccount)
                .enterRecipient("Receiver")
                .enterRecipientAccount(receiverAccount)
                .enterAmount(amount)
                .confirm()
                .sendTransferAndCheckSuccessAlert();
        profile = new UserSteps(
                createdUserRequest.getUsername(),
                createdUserRequest.getPassword(),
                createdUserResponse.getId()
        ).getProfile();
        Account receiverAfter = profile.getAccounts().stream()
                .filter(a -> a.getId().equals(receiver.getId()))
                .findFirst().orElseThrow();
        assert receiverAfter.getBalance().doubleValue() >= amount;
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, 10000.01, -100, -0.01})
    public void userCantTransferAmountInvalidData(double invalidAmount) {
        authAsUser(createdUserRequest);
        UserDashboard dashboard = new UserDashboard().open();
        String senderAccount = dashboard.createAccountAndGetNumber();
        String receiverAccount = dashboard.createAccountAndGetNumber();
        CustomerData profile = new UserSteps(
                createdUserRequest.getUsername(),
                createdUserRequest.getPassword(),
                createdUserResponse.getId()
        ).getProfile();
        Account sender = profile.getAccounts().stream()
                .filter(a -> a.getAccountNumber().contains(senderAccount))
                .findFirst().orElseThrow();
        AccountSteps steps = new AccountSteps(
                createdUserRequest.getUsername(),
                createdUserRequest.getPassword()
        );
        for (int i = 0; i < 10; i++) {
            steps.deposit(sender.getId(), 5000);
        }
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