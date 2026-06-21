package ui.elements;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import static com.codeborne.selenide.Condition.text;


public class UserBadge extends BaseElement {
    private SelenideElement userName;
    private SelenideElement userUsername;

    public UserBadge(SelenideElement container) {
        super(container);
        this.userName = container.find(".user-name");
        this.userUsername = container.find(".user-username");
    }

    public String getName() {
        return userName.getText();
    }

    public String getUsername() {
        return userUsername.getText().replace("@", "");
    }

    public void shouldHaveName(String expectedName) {
        Selenide.refresh();
        userName.shouldHave(text(expectedName));
    }

    public void click() {
        element.click();
    }
}