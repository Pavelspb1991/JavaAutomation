package ui.pages;

import com.codeborne.selenide.SelenideElement;
import lombok.Getter;
import ui.elements.UserBadge;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;


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
}