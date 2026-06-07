package ui.pages;

import com.codeborne.selenide.SelenideElement;
import lombok.Getter;
import org.openqa.selenium.Alert;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;

@Getter
public class TransferPage extends BasePage<TransferPage> {

    private SelenideElement accountSelect = $("select");
    private SelenideElement recipientName = $("[placeholder='Enter recipient name']");
    private SelenideElement recipientAccount = $("[placeholder='Enter recipient account number']");
    private SelenideElement amountInput = $("[placeholder='Enter amount']");
    private SelenideElement confirmCheckbox = $("#confirmCheck");
    private SelenideElement sendButton = $(byText("🚀 Send Transfer"));

    @Override
    public String url() {
        return "/transfer";
    }

    public TransferPage selectSenderAccount(String accountNumber) {
        accountSelect.selectOptionContainingText(accountNumber);
        return this;
    }

    public TransferPage enterRecipient(String name) {
        recipientName.setValue(name);
        return this;
    }

    public TransferPage enterRecipientAccount(String account) {
        recipientAccount.setValue(account);
        return this;
    }

    public TransferPage enterAmount(double amount) {
        amountInput.setValue(String.valueOf(amount));
        return this;
    }

    public TransferPage confirm() {
        confirmCheckbox.setSelected(true);
        return this;
    }

    public TransferPage sendTransfer() {
        sendButton.click();
        return this;
    }

    public TransferPage sendTransferAndCheckSuccessAlert() {
        sendButton.click();
        Alert alert = switchTo().alert();
        String text = alert.getText();
        alert.accept();
        assert text.contains("successful") || text.contains("Success");
        return this;
    }

    public TransferPage sendTransferAndCheckNegativeAlert() {
        sendButton.click();
        Alert alert = switchTo().alert();
        String text = alert.getText();
        alert.accept();
        assert text.contains("must be at least 0.01") ||
                text.contains("fill all fields") ||
                text.contains("cannot exceed 10000") ||
                text.contains("invalid");
        return this;
    }
}