package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;
import org.openqa.selenium.Alert;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;

@Getter
public class UserDashboard extends BasePage<UserDashboard> {
    private SelenideElement welcomeText = $(Selectors.byClassName("welcome-text"));
    private SelenideElement createNewAccount = $(Selectors.byText("➕ Create New Account"));
    private SelenideElement depositMoney = $(Selectors.byText("💰 Deposit Money"));
    private SelenideElement makeTransfer = $(Selectors.byText("🔄 Make a Transfer"));

    public TransferPage goToTransfer() {
        makeTransfer.shouldBe(visible).click();
        return getPage(TransferPage.class);
    }

    public DepositPage goToDeposit() {
        depositMoney.shouldBe(visible).click();
        return getPage(DepositPage.class);
    }

    @Override
    public String url() {
        return "/dashboard";
    }

    public String createAccountAndGetNumber() {
        createNewAccount.shouldBe(visible).click();
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        alert.accept();
        return alertText.replace("✅ New Account Created! Account Number: ", "").trim();
    }
}