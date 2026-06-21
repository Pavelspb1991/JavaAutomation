package ui.pages;

import com.codeborne.selenide.SelenideElement;
import lombok.Getter;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.value;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.refresh;

@Getter
public class EditProfilePage extends BasePage<EditProfilePage> {

    private SelenideElement userNameDisplay = $(".user-name");
    private SelenideElement nameInput = $("[placeholder='Enter new name']");
    private SelenideElement saveButton = $(byText("\uD83D\uDCBE Save Changes")); // 💾

    @Override
    public String url() {
        return "/profile/edit";
    }

    public EditProfilePage changeName(String newName) {
        nameInput.setValue(newName);
        nameInput.shouldHave(value(newName));
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
        refresh();
        userNameDisplay.shouldHave(text(expectedName));
        return this;
    }

    public String getCurrentName() {
        return userNameDisplay.getText();
    }

    public EditProfilePage verifyNameInputHasValue(String expectedValue) {
        nameInput.shouldHave(value(expectedValue));
        return this;
    }

    public EditProfilePage verifyNameInputIsEmpty() {
        nameInput.shouldBe(com.codeborne.selenide.Condition.empty);
        return this;
    }
}