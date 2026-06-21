package common.extensions;

import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.requests.steps.AdminSteps;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import ui.pages.BasePage;
import java.util.LinkedList;
import java.util.List;

public class UserSessionExtension implements BeforeEachCallback {
    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        UserSession annotation = extensionContext.getRequiredTestMethod().getAnnotation(UserSession.class);
        if (annotation != null) {
            int userCount = annotation.value();

            SessionStorage.clear();

            List<CreateUserRequest> users = new LinkedList<>();
            List<CreateUserResponse> responses = new LinkedList<>();

            for (int i = 0; i < userCount; i++) {
                CreateUserRequest user = AdminSteps.generateRandomUserRequest();
                CreateUserResponse response = AdminSteps.createUser(user);
                users.add(user);
                responses.add(response);
            }

            SessionStorage.addUsers(users, responses);

            int authAsUser = annotation.auth();
            BasePage.authAsUser(SessionStorage.getUser(authAsUser + 1));
        }
    }
}