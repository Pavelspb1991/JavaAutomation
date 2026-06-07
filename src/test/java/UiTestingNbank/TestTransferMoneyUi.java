package UiTestingNbank;

import models.Account;
import models.CustomerData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.Alert;
import requests.steps.AccountSteps;
import requests.steps.UserSteps;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.*;

public class TestTransferMoneyUi extends BaseUiTest {

    private static Long senderAccountId;
    private static Long receiverAccountId;

    @BeforeAll
    public static void setupAccountsForTransfer() {
        String username = createdUserRequest.getUsername();
        String password = createdUserRequest.getPassword();
        AccountSteps steps = new AccountSteps(username, password);

        senderAccountId = steps.createAccount().getId();
        receiverAccountId = steps.createAccount().getId();

        for (int i = 0; i < 10; i++) {
            steps.deposit(senderAccountId, 5000);
        }
    }

    @ParameterizedTest
    @ValueSource(doubles = {10000, 0.01, 5000, 9999.99, 5000.50, 0.02})
    public void userCanTransferAmountValidData(double amount) {
        open("/login");
        sleep(2000);
        $("[placeholder='Username']").setValue(createdUserRequest.getUsername());
        $("[placeholder='Password']").setValue(createdUserRequest.getPassword());
        $("button.btn-primary.w-100").click();
        $(".user-username").shouldBe(visible);

        $(byText("\uD83D\uDD04 Make a Transfer")).click();
        $("select").selectOptionContainingText("ACC1");
        $("[placeholder='Enter recipient name']").setValue("Receiver");
        $("[placeholder='Enter recipient account number']").setValue("ACC2");
        $("[placeholder='Enter amount']").setValue(String.valueOf(amount));
        $("#confirmCheck").setSelected(true);
        $(byText("\uD83D\uDE80 Send Transfer")).click();

        Alert alert = switchTo().alert();
        String text = alert.getText();
        alert.accept();
        assert text.contains("successful") || text.contains("Success")
                : "Неожиданный текст: " + text;
        // Проверка на уровне api. Исходный код в классе - TestTransferMoney
        AccountSteps steps = new AccountSteps(
                createdUserRequest.getUsername(),
                createdUserRequest.getPassword()
        );
        CustomerData profile = new UserSteps(
                createdUserRequest.getUsername(),
                createdUserRequest.getPassword(),
                createdUserResponse.getId()
        ).getProfile();

        Account sender = profile.getAccounts().stream()
                .filter(a -> a.getId().equals(senderAccountId))
                .findFirst().orElseThrow();
        Account receiver = profile.getAccounts().stream()
                .filter(a -> a.getId().equals(receiverAccountId))
                .findFirst().orElseThrow();

        assert receiver.getBalance().doubleValue() >= amount;
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, 10000.01, -100, -0.01})
    public void userCantTransferAmountInvalidData(double invalidAmount) {
        open("/login");
        sleep(2000);
        $("[placeholder='Username']").setValue(createdUserRequest.getUsername());
        $("[placeholder='Password']").setValue(createdUserRequest.getPassword());
        $("button.btn-primary.w-100").click();
        $(".user-username").shouldBe(visible);

        $(byText("\uD83D\uDD04 Make a Transfer")).click();
        $("select").selectOptionContainingText("ACC1");
        $("[placeholder='Enter recipient name']").setValue("Receiver");
        $("[placeholder='Enter recipient account number']").setValue("ACC2");
        $("[placeholder='Enter amount']").setValue(String.valueOf(invalidAmount));
        $("#confirmCheck").setSelected(true);
        $(byText("\uD83D\uDE80 Send Transfer")).click();

        Alert alert = switchTo().alert();
        String text = alert.getText();
        alert.accept();
        if (invalidAmount == 0 || invalidAmount == -100 || invalidAmount == -0.01) {
            assert text.contains("must be at least 0.01") || text.contains("fill all fields");
        } else if (invalidAmount == 10000.01) {
            assert text.contains("cannot exceed 10000");
        } else {
            assert text.contains("invalid") || text.contains("Invalid");
        }
    }
}