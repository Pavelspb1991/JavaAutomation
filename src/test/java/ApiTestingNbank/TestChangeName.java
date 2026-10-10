package ApiTestingNbank;

import api.models.*;
import api.models.comparison.ModelAssertions;
import common.annotations.UserSession;
import common.extensions.UserSessionExtension;
import common.storage.SessionStorage;
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

    @ParameterizedTest
    @UserSession
    @ValueSource(strings = {"A B", "Ab Cd", "Abcdefghijklmno Abcdefghijklmn"})
    public void userCanChangeNameWithValidData(String validName) {
        UpdateCustomerProfileRequest request = UpdateCustomerProfileRequest.builder()
                .name(validName)
                .build();
        UpdateCustomerProfileResponse response = SessionStorage.getSteps().updateName(request);
        ModelAssertions.assertThatModels(request, response).match();

        CustomerData profile = SessionStorage.getSteps().getProfile();
        softly.assertThat(profile.getName()).isEqualTo(validName);
    }

    @ParameterizedTest
    @UserSession
    @ValueSource(strings = {"A", "AbCd", "John1 Doe", "John Doe1", "John1 Doe Jack", "Привет", ""})
    public void userCantChangeNameWithInvalidData(String invalidName) {
        UpdateCustomerProfileRequest request = UpdateCustomerProfileRequest.builder()
                .name(invalidName)
                .build();

        String errorMessage = new ValidatedCrudRequester<UpdateCustomerProfileResponse>(
                RequestSpecs.authAsUser(
                        SessionStorage.getUser().getUsername(),
                        SessionStorage.getUser().getPassword()),
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.invalidDataProvided()
        ).putExpectingErrorWithBody(SessionStorage.getUserResponse().getId(), request);

        softly.assertThat(errorMessage).isEqualTo(ErrorMessages.INVALID_NAME);

        CustomerData profile = SessionStorage.getSteps().getProfile();
        softly.assertThat(profile.getName()).isNotEqualTo(invalidName);
    }

    @Test
    @UserSession
    public void userCantChangeNameWithoutToken() {
        String originalName = SessionStorage.getSteps().getProfile().getName();
        String newName = UserSteps.generateRandomValidName();
        SessionStorage.getSteps().updateNameWithoutAuth(newName);

        CustomerData getResponse = SessionStorage.getSteps().getProfile();
        softly.assertThat(getResponse.getName()).isEqualTo(originalName);
    }
}