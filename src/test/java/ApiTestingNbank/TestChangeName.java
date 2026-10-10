package ApiTestingNbank;

import api.dao.UserDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.models.*;
import api.models.comparison.ModelAssertions;
import api.requests.steps.DataBaseSteps;
import common.annotations.UserSession;
import common.extensions.UserSessionExtension;
import common.storage.SessionStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

@ExtendWith(UserSessionExtension.class)
public class TestChangeName extends BaseTest {

    private UserSteps userSteps;
    private CreateUserRequest createdUserRequest;
    private CreateUserResponse createdUserResponse;

    @BeforeEach
    public void initUserSteps() {
        createdUserRequest = SessionStorage.getUser();
        createdUserResponse = SessionStorage.getUserResponse();

        userSteps = new UserSteps(
                createdUserRequest.getUsername(),
                createdUserRequest.getPassword(),
                createdUserResponse.getId()
        );
    }

    @ParameterizedTest
    @UserSession
    @ValueSource(strings = {"A B", "Ab Cd", "Abcdefghijklmno Abcdefghijklmn"})
    public void userCanChangeNameWithValidData(String validName) {
        UpdateCustomerProfileRequest request = UpdateCustomerProfileRequest.builder()
                .name(validName)
                .build();
        UpdateCustomerProfileResponse response = userSteps.updateName(request);
        ModelAssertions.assertThatModels(request, response).match();

        CustomerData profile = userSteps.getProfile();
        softly.assertThat(profile.getName()).isEqualTo(validName);

        UserDao userDao = DataBaseSteps.getUserByUsername(createdUserRequest.getUsername());
        DaoAndModelAssertions.assertThat(response, userDao).match();
    }

    @ParameterizedTest
    @UserSession
    @ValueSource(strings = {"A", "AbCd", "John1 Doe", "John Doe1", "John1 Doe Jack", "Привет", ""})
    public void userCantChangeNameWithInvalidData(String invalidName) {
        UpdateCustomerProfileRequest request = UpdateCustomerProfileRequest.builder()
                .name(invalidName)
                .build();

        String errorMessage = new ValidatedCrudRequester<UpdateCustomerProfileResponse>(
                RequestSpecs.authAsUser(createdUserRequest.getUsername(), createdUserRequest.getPassword()),
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.invalidDataProvided()
        ).putExpectingErrorWithBody(createdUserResponse.getId(), request);

        softly.assertThat(errorMessage).isEqualTo(ErrorMessages.INVALID_NAME);

        CustomerData profile = userSteps.getProfile();
        softly.assertThat(profile.getName()).isNotEqualTo(invalidName);
    }

    @Test
    @UserSession
    public void userCantChangeNameWithoutToken() {
        String originalName = userSteps.getProfile().getName();
        String newName = UserSteps.generateRandomValidName();
        userSteps.updateNameWithoutAuth(newName);

        CustomerData getResponse = userSteps.getProfile();
        softly.assertThat(getResponse.getName()).isEqualTo(originalName);
    }
}