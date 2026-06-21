package UiTestingNbank;

import api.models.CustomerData;
import api.requests.steps.UserSteps;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ui.pages.BankAlert;
import ui.pages.EditProfilePage;
import ui.pages.LoginPage;

public class TestChangeUiName extends BaseUiTest {

    @ParameterizedTest
    @ValueSource(strings = {"A B", "Ab Cd", "Abcdefghijklmno Abcdefghijklmn"})
    public void changeUserNameTest(String validName) {
        authAsUser(createdUserRequest.getUsername(),createdUserRequest.getPassword());
        new LoginPage()
                .verifyLoginAndGoToProfile(createdUserRequest.getUsername());
        new EditProfilePage()
                .changeName(validName)
                .clickSave()
                .checkAlertMessageAndAccept(BankAlert.NAME_UPDATED_SUCCESSFULLY.getMessage())
                .verifyNameDisplayed(validName)
                .clearName();

        UserSteps userSteps = new UserSteps(
                createdUserRequest.getUsername(),
                createdUserRequest.getPassword(),
                createdUserResponse.getId()
        );
        CustomerData profile = userSteps.getProfile();
        assert profile.getName().equals(validName);
    }

    @ParameterizedTest
    @ValueSource(strings = {"A", "AbCd", "John1 Doe", "John Doe1", "John1 Doe Jack", "Привет", ""})
    public void userCantChangeNameWithInvalidData(String invalidName) {
        authAsUser(createdUserRequest.getUsername(),createdUserRequest.getPassword());
        new LoginPage()
                .verifyLoginAndGoToProfile(createdUserRequest.getUsername());
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