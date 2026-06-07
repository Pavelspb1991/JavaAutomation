package UiTestingNbank;

import models.CustomerData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.Alert;
import requests.steps.UserSteps;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.*;

public class TestChangeUiName extends BaseUiTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "A B",
            "Ab Cd",
            "Abcdefghijklmno Abcdefghijklmn"
    })
    public void changeUserNameTest(String validName) {
        open("/login");
        $("[placeholder='Username']").setValue(createdUserRequest.getUsername());
        $("[placeholder='Password']").setValue(createdUserRequest.getPassword());
        $("button.btn-primary.w-100").click();
        $(".user-username").shouldBe(visible)
                .shouldHave(text("@" + createdUserRequest.getUsername()));
        $(".user-username").click();
        $("[placeholder='Enter new name']")
                .setValue(validName)
                .shouldHave(value(validName));
        $(byText("\uD83D\uDCBE Save Changes")).click();
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        alert.accept();
        assert alertText.equals("✅ Name updated successfully!");
        refresh();
        $(".user-name").shouldHave(text(validName));
        $("[placeholder='Enter new name']").clear();

        // Проверка на уровне api. Исходный код в классе - TestChangeName
        UserSteps userSteps = new UserSteps(
                createdUserRequest.getUsername(),
                createdUserRequest.getPassword(),
                createdUserResponse.getId()
        );
        CustomerData profile = userSteps.getProfile();
        assert profile.getName().equals(validName)
                : "Имя на бэке " + profile.getName() + " != " + validName;

    }

    @ParameterizedTest
    @ValueSource(strings = {
            "A", "AbCd", "John1 Doe", "John Doe1", "John1 Doe Jack", "Привет", ""
    })
    public void userCantChangeNameWithInvalidData(String invalidName) {
        open("/login");
        sleep(2000);
        $("[placeholder='Username']").setValue(createdUserRequest.getUsername());
        $("[placeholder='Password']").setValue(createdUserRequest.getPassword());
        $("button.btn-primary.w-100").click();
        $(".user-username")
                .shouldBe(visible)
                .shouldHave(text("@" + createdUserRequest.getUsername()));
        $(".user-username").click();
        String originalName = $(".user-name").getText();
        $("[placeholder='Enter new name']").clear();
        $("[placeholder='Enter new name']").setValue(invalidName);


        if (!invalidName.isEmpty()) {
            $("[placeholder='Enter new name']").shouldHave(value(invalidName));
        } else {
            $("[placeholder='Enter new name']").shouldBe(empty);
        }

        $(byText("\uD83D\uDCBE Save Changes")).click();
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        alert.accept();
        assert alertText.contains("Name must contain two words with letters only")
                || alertText.contains("❌ Please enter a valid name");
        refresh();
        $(".user-name").shouldHave(text(originalName));
        $("[placeholder='Enter new name']").clear();
    }
}