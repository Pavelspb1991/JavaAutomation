package ApiTestingNbank;
import generators.RandomData;
import models.CreateUserRequest;
import models.CreateUserResponse;
import models.UserRole;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import requests.CreateNewUserRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class BaseTest {
    protected SoftAssertions softly;
    protected static CreateUserRequest createdUserRequest;
    protected static CreateUserResponse createdUserResponse;

    @BeforeEach
    public void setupTest() {
        this.softly = new SoftAssertions();
    }

    @AfterEach
    public void afterTest() {
        softly.assertAll();
    }

    //Создаем Юзера перед тестами, вынес создание в базовый класс
    @BeforeAll
    public static void createUser() throws InterruptedException {
        createdUserRequest = CreateUserRequest.
                builder().username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        createdUserResponse  = new CreateNewUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated()).post(createdUserRequest).extract().as(CreateUserResponse.class);

    }
}