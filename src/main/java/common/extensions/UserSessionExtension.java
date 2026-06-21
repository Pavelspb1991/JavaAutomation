package common.extensions;

import api.models.CreateUserRequest;
import common.annotations.UserSession;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import ui.pages.BasePage;

public class UserSessionExtension implements BeforeEachCallback {
    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        UserSessionExtension annotation = extensionContext.getRequiredTestMethod().getAnnotation(UserSession.class);
        if (annotation != null) {
            BasePage.authAsUser(CreateUserRequest.getAdmin());
        }
    }
}
