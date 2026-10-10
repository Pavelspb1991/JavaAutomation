package UiTestingNbank;

import api.models.CustomerData;
import common.annotations.Browsers;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ui.pages.BankAlert;
import ui.pages.EditProfilePage;
import ui.pages.LoginPage;
import api.models.CreateUserRequest;

public class TestChangeUiName extends BaseUiTest {

    @ParameterizedTest
    @UserSession
    @Browsers({"chrome"})
    @ValueSource(strings = {"A B", "Ab Cd", "Abcdefghijklmno Abcdefghijklmn"})
    public void changeUserNameTest(String validName) {
        CreateUserRequest user = SessionStorage.getUser();
        new LoginPage()
                .verifyLoginAndGoToProfile(user.getUsername());

        new EditProfilePage()
                .changeName(validName)
                .clickSave()
                .checkAlertMessageAndAccept(BankAlert.NAME_UPDATED_SUCCESSFULLY.getMessage())
                .verifyNameDisplayed(validName)
                .clearName();

        CustomerData profile = SessionStorage.getSteps().getProfile();
        assert profile.getName().equals(validName);
    }

    @ParameterizedTest
    @UserSession
    @Browsers({"chrome"})
    @ValueSource(strings = {"A", "AbCd", "John1 Doe", "John Doe1", "John1 Doe Jack", "Привет", ""})
    public void userCantChangeNameWithInvalidData(String invalidName) {
        CreateUserRequest user = SessionStorage.getUser();
        new LoginPage()
                .verifyLoginAndGoToProfile(user.getUsername());

        EditProfilePage editPage = new EditProfilePage();
        String originalName = editPage.getCurrentName();
        editPage.clearName()
                .changeName(invalidName);

        if (!invalidName.isEmpty()) {
            editPage.verifyNameInputHasValue(invalidName);
        } else {
            editPage.verifyNameInputIsEmpty();
        }
        editPage.clickSave();
        editPage.checkAlertMessageAndAccept(BankAlert.NAME_MUST_CONTAIN_TWO_WORDS.getMessage());
        editPage.verifyNameDisplayed(originalName)
                .clearName();
    }
}