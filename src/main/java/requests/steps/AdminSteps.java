package requests.steps;
import generators.RandomModelGenerator;
import models.CreateUserRequest;
import models.CreateUserResponse;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;
import generators.RandomData;
import models.UserRole;

public class AdminSteps {

    public static CreateUserRequest generateRandomUserRequest() {
        CreateUserRequest request = RandomModelGenerator.generate(CreateUserRequest.class);
        request.setRole(UserRole.USER);
        return request;
    }

    public static CreateUserResponse createUser(CreateUserRequest request) {
        return new ValidatedCrudRequester<CreateUserResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpecs.entityWasCreated()
        ).post(request);
    }
}