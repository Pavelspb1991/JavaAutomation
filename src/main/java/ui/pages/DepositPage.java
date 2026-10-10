package ui.pages;

import com.codeborne.selenide.SelenideElement;
import lombok.Getter;
import org.openqa.selenium.Alert;
import ui.elements.AmountInput;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;

@Getter
public class DepositPage extends BasePage<DepositPage> {

    private SelenideElement accountSelect = $("select");
    private SelenideElement depositButton = $(byText("💵 Deposit"));
    private AmountInput amountInput = new AmountInput($("[placeholder='Enter amount']"));

    @Override
    public String url() {
        return "/deposit";
    }

    public DepositPage selectAccount(String accountNumber) {
        accountSelect.shouldHave(text("-- Choose an account --"));
        accountSelect.selectOptionContainingText(accountNumber);
        return this;
    }

    public DepositPage enterAmount(double amount) {
        amountInput.setValue(amount);
        amountInput.shouldHaveValue(amount);
        return this;
    }

    public DepositPage clickDeposit() {
        depositButton.click();
        return this;
    }

    public void clickDepositAndCheckNegativeAlert() {
        depositButton.click();
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        alert.accept();

        assert alertText.contains(BankAlert.PLEASE_ENTER_VALID_AMOUNT.getMessage()) ||
                alertText.contains(BankAlert.DEPOSIT_LIMIT_EXCEEDED.getMessage());
    }
}