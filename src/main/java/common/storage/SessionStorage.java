package common.storage;

import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.requests.steps.UserSteps;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class SessionStorage {

    private static final ThreadLocal<LinkedHashMap<CreateUserRequest, UserSteps>> userStepsMap =
            ThreadLocal.withInitial(LinkedHashMap::new);
    private static final ThreadLocal<List<CreateUserResponse>> userResponses =
            ThreadLocal.withInitial(ArrayList::new);

    private SessionStorage() {}

    public static void addUsers(List<CreateUserRequest> users, List<CreateUserResponse> responses) {
        userStepsMap.get().clear();
        userResponses.get().clear();

        for (int i = 0; i < users.size(); i++) {
            CreateUserRequest user = users.get(i);
            CreateUserResponse response = responses.get(i);
            userStepsMap.get().put(
                    user,
                    new UserSteps(user.getUsername(), user.getPassword(), response.getId())
            );
            userResponses.get().add(response);
        }
    }

    public static CreateUserRequest getUser(int number) {
        return new ArrayList<>(userStepsMap.get().keySet()).get(number - 1);
    }

    public static CreateUserRequest getUser() {
        return getUser(1);
    }

    public static UserSteps getSteps(int number) {
        return new ArrayList<>(userStepsMap.get().values()).get(number - 1);
    }

    public static UserSteps getSteps() {
        return getSteps(1);
    }

    public static CreateUserResponse getUserResponse(int number) {
        return userResponses.get().get(number - 1);
    }

    public static CreateUserResponse getUserResponse() {
        return getUserResponse(1);
    }

    public static void clear() {
        userStepsMap.get().clear();
        userResponses.get().clear();
    }
}