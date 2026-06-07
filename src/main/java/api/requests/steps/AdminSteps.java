package api.requests.steps;
import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import api.models.UserRole;

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