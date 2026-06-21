package ui.pages;

import com.codeborne.selenide.SelenideElement;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class LoginPage extends BasePage<LoginPage> {
    private SelenideElement button = $("button");
    private SelenideElement userMenu = $(".user-username");

    @Override
    public String url() {
        return "/login";
    }

    public LoginPage login(String username, String password) {
        usernameInput.setValue(username);
        passwordInput.setValue(password);
        button.click();
        return this;
    }

    public EditProfilePage verifyLoginAndGoToProfile(String expectedUsername) {
        userMenu.shouldBe(visible)
                .shouldHave(text("@" + expectedUsername));
        userMenu.click();
        return getPage(EditProfilePage.class);
    }
}