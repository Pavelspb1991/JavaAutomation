package ApiTestingNbank;
import api.models.ErrorMessages;
import api.models.UpdateCustomerProfileRequest;
import api.models.UpdateCustomerProfileResponse;
import api.models.comparison.ModelAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import api.models.CustomerData;

public class TestChangeName extends BaseTest {
    private UserSteps userSteps;

    // Метод инициализирует UserSteps с данными тестового пользователя.
    @BeforeEach
    public void initUserSteps() {
        userSteps = new UserSteps(
                createdUserRequest.getUsername(),
                createdUserRequest.getPassword(),
                createdUserResponse.getId()
        );
    }

    //Метод проверяет возможность изменения имени пользователя (валидные значения)
    //Источник входных данных - @ValueSource
    @ParameterizedTest
    @ValueSource(strings = {
            "A B",
            "Ab Cd",
            "Abcdefghijklmno Abcdefghijklmn"
    })
    public void userCanChangeNameWithValidData(String validName) {
        UpdateCustomerProfileRequest request = UpdateCustomerProfileRequest.builder()
                .name(validName)
                .build();
        UpdateCustomerProfileResponse response = userSteps.updateName(request);
        ModelAssertions.assertThatModels(request, response).match();

        CustomerData profile = userSteps.getProfile();
        softly.assertThat(profile.getName()).isEqualTo(validName);
    }

    //Тест на проверку невозможности изменения имени пользователя при невалидных данных
    //Источник входных данных - @ValueSource
    @ParameterizedTest
    @ValueSource(strings = {
            "A", "AbCd", "John1 Doe", "John Doe1", "John1 Doe Jack", "Привет", ""
    })
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

    //Тест на проверку невозможности изменения имени пользователя без авторизации
    @Test
    public void userCantChangeNameWithoutToken() {
        String originalName = userSteps.getProfile().getName();
        String newName = UserSteps.generateRandomValidName();
        userSteps.updateNameWithoutAuth(newName);

        CustomerData getResponse = userSteps.getProfile();
        softly.assertThat(getResponse.getName()).isEqualTo(originalName);
    }
}