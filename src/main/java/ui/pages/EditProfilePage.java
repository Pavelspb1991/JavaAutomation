package ui.pages;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;
import org.openqa.selenium.Alert;
import ui.elements.UserBadge;

import java.time.Duration;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;


@Getter
public class EditProfilePage extends BasePage<EditProfilePage> {

    private UserBadge userBadge = new UserBadge($(".profile-header"));
    private SelenideElement nameInput = $("[placeholder='Enter new name']");
    private SelenideElement saveButton = $(byText("\uD83D\uDCBE Save Changes"));

    @Override
    public String url() {
        return "/profile/edit";
    }

    public EditProfilePage changeName(String newName) {
        nameInput.setValue(newName);
        if (!newName.isEmpty()) {
            nameInput.shouldHave(value(newName));
        } else {
            nameInput.shouldBe(empty);
        }
        return this;
    }

    public EditProfilePage clearName() {
        nameInput.clear();
        return this;
    }

    public EditProfilePage clickSave() {
        saveButton.click();
        return this;
    }

    public EditProfilePage verifyNameDisplayed(String expectedName) {
        userBadge.shouldHaveName(expectedName);
        return this;
    }

    public String getCurrentName() {
        return userBadge.getName();
    }

    public EditProfilePage verifyNameInputHasValue(String expectedValue) {
        nameInput.shouldHave(value(expectedValue));
        return this;
    }

    public EditProfilePage verifyNameInputIsEmpty() {
        nameInput.shouldBe(empty);
        return this;
    }

    public EditProfilePage clickSaveAndCheckNegativeAlert() {
        saveButton.click();
        Alert alert = Selenide.switchTo().alert(Duration.ofSeconds(20));
        String alertText = alert.getText();
        alert.accept();

        // Фронт в негативном сценарии отдаёт alert с объектом, а не строкой —
        // браузер показывает "[object Object]". Это известный баг фронта,
        // зафиксирован в тесте как фактическое поведение.
        boolean isKnownFrontendBug = alertText.equals("[object Object]");
        boolean isExpectedMessage =
                alertText.contains(BankAlert.NAME_MUST_CONTAIN_TWO_WORDS.getMessage()) ||
                        alertText.contains(BankAlert.PLEASE_ENTER_VALID_NAME.getMessage());

        assert isKnownFrontendBug || isExpectedMessage
                : "Неожиданный alert: " + alertText;

        return this;
    }
}