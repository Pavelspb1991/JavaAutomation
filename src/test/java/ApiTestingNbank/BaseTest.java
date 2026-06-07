package ApiTestingNbank;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import api.requests.steps.AdminSteps;

public abstract class BaseTest {

    protected static CreateUserRequest createdUserRequest;
    protected static CreateUserResponse createdUserResponse;
    protected SoftAssertions softly;

    @BeforeAll
    public static void setUp() {
        // Используем AdminSteps для создания пользователя
        createdUserRequest = AdminSteps.generateRandomUserRequest();
        createdUserResponse = AdminSteps.createUser(createdUserRequest);
    }

    @BeforeEach
    public void initSoftly() {
        softly = new SoftAssertions();
    }

    @AfterEach
    public void assertSoftly() {
        softly.assertAll();
    }
}