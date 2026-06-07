package UiTestingNbank;

import com.codeborne.selenide.Configuration;
import models.CreateUserRequest;
import models.CreateUserResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.openqa.selenium.chrome.ChromeOptions;
import requests.steps.AdminSteps;
import java.util.Map;

import static com.codeborne.selenide.Selenide.closeWebDriver;

public abstract class BaseUiTest {

    protected static CreateUserRequest createdUserRequest;
    protected static CreateUserResponse createdUserResponse;

    @BeforeAll
    public static void setupSelenoidAndCreateUser() {
        ChromeOptions options = new ChromeOptions();
        options.setCapability("selenoid:options",
                Map.of("enableVNC", true, "enableLog", true));
        Configuration.browserCapabilities = options;
        Configuration.remote = "http://127.0.0.1:4444/wd/hub";
        Configuration.baseUrl = "http://192.168.50.9:3000";
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";

        createdUserRequest = AdminSteps.generateRandomUserRequest();
        createdUserResponse = AdminSteps.createUser(createdUserRequest);
    }

    @AfterEach
    public void closeBrowser() {
        closeWebDriver();
    }
}