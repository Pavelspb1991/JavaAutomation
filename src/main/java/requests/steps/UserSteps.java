package requests.steps;

import models.CustomerData;
import models.UpdateCustomerProfileRequest;
import models.UpdateCustomerProfileResponse;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;
import io.restassured.specification.ResponseSpecification;
import java.util.Random;

public class UserSteps {
    private final String username;
    private final String password;
    private final long userId;

    public UserSteps(String username, String password, long userId) {
        this.username = username;
        this.password = password;
        this.userId = userId;
    }

    public UpdateCustomerProfileResponse updateName(UpdateCustomerProfileRequest request) {
        return new ValidatedCrudRequester<UpdateCustomerProfileResponse>(
                RequestSpecs.authAsUser(username, password),
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.ok()
        ).put(userId, request);
    }

    public CustomerData getProfile() {
        return new ValidatedCrudRequester<CustomerData>(
                RequestSpecs.authAsUser(username, password),
                Endpoint.CUSTOMER_PROFILE_GET,
                ResponseSpecs.ok()
        ).get();
    }

    public void updateNameExpectingError(String newName, ResponseSpecification errorSpec) {
        UpdateCustomerProfileRequest request = UpdateCustomerProfileRequest.builder()
                .name(newName)
                .build();

        new ValidatedCrudRequester<UpdateCustomerProfileResponse>(
                RequestSpecs.authAsUser(username, password),
                Endpoint.CUSTOMER_PROFILE,
                errorSpec
        ).put(userId, request);
    }

    public void updateNameWithoutAuth(String newName) {
        UpdateCustomerProfileRequest request = UpdateCustomerProfileRequest.builder()
                .name(newName)
                .build();

        new ValidatedCrudRequester<UpdateCustomerProfileResponse>(
                RequestSpecs.unauthSpec(),
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.invalidToken()
        ).putExpectingError(userId, request);
    }

    public static String generateRandomValidName() {
        String[] firstNames = {"John", "Jane", "Bob", "Alice", "Mike", "Sarah", "David", "Emma"};
        String[] lastNames = {"Smith", "Johnson", "Brown", "Taylor", "Anderson", "Wilson", "Martin"};
        Random random = new Random();
        return firstNames[random.nextInt(firstNames.length)] + " " +
                lastNames[random.nextInt(lastNames.length)];
    }
}