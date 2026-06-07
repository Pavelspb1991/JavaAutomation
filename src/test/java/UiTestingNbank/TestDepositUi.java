package UiTestingNbank;

import models.Account;
import models.CustomerData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.Alert;
import requests.steps.AccountSteps;
import requests.steps.UserSteps;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.*;

public class TestDepositUi extends BaseUiTest {

    @ParameterizedTest
    @ValueSource(doubles = {2500, 0.01, 5000, 4999.99})
    public void userCanDepositMoneyValidData(double amount) {
        open("/login");
        sleep(2000);
        $("[placeholder='Username']").setValue(createdUserRequest.getUsername());
        $("[placeholder='Password']").setValue(createdUserRequest.getPassword());
        $("button.btn-primary.w-100").click();
        $(".user-username").shouldBe(visible)
                .shouldHave(text("@" + createdUserRequest.getUsername()));
        $(byText("➕ Create New Account")).shouldBe(visible);
        $(byText("➕ Create New Account")).click();
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        alert.accept();
        assert alertText.contains("✅ New Account Created! Account Number");
        $(byText("\uD83D\uDCB0 Deposit Money")).click();
        $("select").shouldHave(text("-- Choose an account --"));
        $("select").selectOptionContainingText("ACC1");
        $("[placeholder='Enter amount']")
                .setValue(String.valueOf(amount))
                .shouldHave(value(String.valueOf(amount)));
        $(byText("\uD83D\uDCB5 Deposit")).click();
        Alert depositAlert = switchTo().alert();
        String depositAlertText = depositAlert.getText();
        depositAlert.accept();
        assert depositAlertText.contains("Successfully deposited");
        assert depositAlertText.contains("$" + amount);

        AccountSteps accountSteps = new AccountSteps(
                createdUserRequest.getUsername(),
                createdUserRequest.getPassword()
        );
        CustomerData profile = new UserSteps(
                createdUserRequest.getUsername(),
                createdUserRequest.getPassword(),
                createdUserResponse.getId()
        ).getProfile();
        Account account = profile.getAccounts().stream()
                .filter(a -> a.getAccountNumber().contains("ACC1"))
                .findFirst()
                .orElseThrow();
        assert account.getBalance().doubleValue() == amount
                : "Баланс " + account.getBalance() + " != " + amount;
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, -100, -0.01, 5000.01})
    public void userCantDepositMoneyWithInvalidData(double invalidAmount) {
        open("/login");
        sleep(2000);
        $("[placeholder='Username']").setValue(createdUserRequest.getUsername());
        $("[placeholder='Password']").setValue(createdUserRequest.getPassword());
        $("button.btn-primary.w-100").click();
        $(".user-username").shouldBe(visible)
                .shouldHave(text("@" + createdUserRequest.getUsername()));
        $(byText("➕ Create New Account")).click();
        Alert createAccountAlert = switchTo().alert();
        String createAccountText = createAccountAlert.getText();
        createAccountAlert.accept();
        assert createAccountText.contains("✅ New Account Created!");
        $(byText("\uD83D\uDCB0 Deposit Money")).click();
        $("select").shouldBe(visible);
        $("select").selectOptionContainingText("ACC1");

        $("[placeholder='Enter amount']").clear();
        $("[placeholder='Enter amount']").setValue(String.valueOf(invalidAmount));

        $("button.btn-primary.shadow-custom.mt-4").click();

        Alert depositAlert = switchTo().alert();
        String depositAlertText = depositAlert.getText();
        depositAlert.accept();
        assert depositAlertText.contains("❌ Please enter a valid amount.") ||
                depositAlertText.contains("❌ Please deposit less or equal to 5000$");
    }
}