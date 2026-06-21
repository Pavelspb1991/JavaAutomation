package common.storage;

import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.requests.steps.UserSteps;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class SessionStorage {
    private static final SessionStorage INSTANCE = new SessionStorage();

    private final LinkedHashMap<CreateUserRequest, UserSteps> userStepsMap = new LinkedHashMap<>();
    private final List<CreateUserResponse> userResponses = new ArrayList<>();

    private SessionStorage() {}

    public static void addUsers(List<CreateUserRequest> users, List<CreateUserResponse> responses) {
        INSTANCE.userStepsMap.clear();
        INSTANCE.userResponses.clear();

        for (int i = 0; i < users.size(); i++) {
            CreateUserRequest user = users.get(i);
            CreateUserResponse response = responses.get(i);
            INSTANCE.userStepsMap.put(
                    user,
                    new UserSteps(user.getUsername(), user.getPassword(), response.getId())
            );
            INSTANCE.userResponses.add(response);
        }
    }

    public static CreateUserRequest getUser(int number) {
        return new ArrayList<>(INSTANCE.userStepsMap.keySet()).get(number - 1);
    }

    public static CreateUserRequest getUser() {
        return getUser(1);
    }

    public static UserSteps getSteps(int number) {
        return new ArrayList<>(INSTANCE.userStepsMap.values()).get(number - 1);
    }

    public static UserSteps getSteps() {
        return getSteps(1);
    }

    public static CreateUserResponse getUserResponse(int number) {
        return INSTANCE.userResponses.get(number - 1);
    }
    public static CreateUserResponse getUserResponse() {
        return getUserResponse(1);
    }

    public static void clear() {
        INSTANCE.userStepsMap.clear();
        INSTANCE.userResponses.clear();
    }
}