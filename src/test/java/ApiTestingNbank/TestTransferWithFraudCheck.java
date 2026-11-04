package ApiTestingNbank;

import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.models.TransferResponse;
import api.models.TransferStatus;
import api.requests.steps.AccountSteps;
import common.annotations.FraudCheckMock;
import common.annotations.UserSession;
import common.extensions.FraudCheckWireMockExtension;
import common.extensions.UserSessionExtension;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({UserSessionExtension.class, FraudCheckWireMockExtension.class})
public class TestTransferWithFraudCheck extends BaseTest {

    @Test
    @UserSession(2)
    @FraudCheckMock
    public void transferApprovedByFraudCheck() {
        CreateUserRequest user1 = SessionStorage.getUser(1);
        CreateUserRequest user2 = SessionStorage.getUser(2);

        AccountSteps user1Steps = new AccountSteps(user1.getUsername(), user1.getPassword());
        AccountSteps user2Steps = new AccountSteps(user2.getUsername(), user2.getPassword());

        CreateAccountResponse account1 = user1Steps.createAccount();
        CreateAccountResponse account2 = user2Steps.createAccount();

        user1Steps.deposit(account1.getId(), 5000);

        TransferResponse response = user1Steps.transferWithFraudCheck(
                account1.getId(),
                account2.getId(),
                1000
        );

        softly.assertThat(response.getStatus()).isEqualTo(TransferStatus.APPROVED.getValue());
        softly.assertThat(response.getRequiresManualReview()).isFalse();
        softly.assertThat(response.getRequiresVerification()).isFalse();
    }

    @Test
    @UserSession(2)
    @FraudCheckMock(
            decision = "BLOCKED",
            riskScore = 0.95,
            reason = "Suspicious activity"
    )
    public void transferBlockedByFraudCheck() {
        CreateUserRequest user1 = SessionStorage.getUser(1);
        CreateUserRequest user2 = SessionStorage.getUser(2);

        AccountSteps user1Steps = new AccountSteps(user1.getUsername(), user1.getPassword());
        AccountSteps user2Steps = new AccountSteps(user2.getUsername(), user2.getPassword());

        CreateAccountResponse account1 = user1Steps.createAccount();
        CreateAccountResponse account2 = user2Steps.createAccount();

        user1Steps.deposit(account1.getId(), 5000);

        TransferResponse response = user1Steps.transferWithFraudCheck(
                account1.getId(),
                account2.getId(),
                1000
        );

        softly.assertThat(response.getStatus()).isEqualTo(TransferStatus.BLOCKED.getValue());
        softly.assertThat(response.getRequiresManualReview()).isFalse();
        softly.assertThat(response.getRequiresVerification()).isFalse();
    }

    @Test
    @UserSession(2)
    @FraudCheckMock(
            status = "SUCCESS",
            decision = "REVIEW_REQUIRED",
            riskScore = 0.7,
            requiresManualReview = true
    )
    public void transferRequiresManualReview() {
        CreateUserRequest user1 = SessionStorage.getUser(1);
        CreateUserRequest user2 = SessionStorage.getUser(2);

        AccountSteps user1Steps = new AccountSteps(user1.getUsername(), user1.getPassword());
        AccountSteps user2Steps = new AccountSteps(user2.getUsername(), user2.getPassword());

        CreateAccountResponse account1 = user1Steps.createAccount();
        CreateAccountResponse account2 = user2Steps.createAccount();

        user1Steps.deposit(account1.getId(), 5000);

        TransferResponse response = user1Steps.transferWithFraudCheck(
                account1.getId(),
                account2.getId(),
                1000
        );

        softly.assertThat(response.getStatus()).isEqualTo(TransferStatus.MANUAL_REVIEW_REQUIRED.getValue());
        softly.assertThat(response.getRequiresManualReview()).isTrue();
    }

    @Test
    @UserSession(2)
    @FraudCheckMock(
            status = "SUCCESS",
            decision = "VERIFICATION_REQUIRED",
            riskScore = 0.6,
            reason = "Additional verification required",
            additionalVerificationRequired = true
    )
    public void transferRequiresVerification() {
        CreateUserRequest user1 = SessionStorage.getUser(1);
        CreateUserRequest user2 = SessionStorage.getUser(2);

        AccountSteps user1Steps = new AccountSteps(user1.getUsername(), user1.getPassword());
        AccountSteps user2Steps = new AccountSteps(user2.getUsername(), user2.getPassword());

        CreateAccountResponse account1 = user1Steps.createAccount();
        CreateAccountResponse account2 = user2Steps.createAccount();

        user1Steps.deposit(account1.getId(), 5000);

        TransferResponse response = user1Steps.transferWithFraudCheck(
                account1.getId(),
                account2.getId(),
                1000
        );

        softly.assertThat(response.getRequiresVerification()).isTrue();
    }

    @Test
    @UserSession(2)
    @FraudCheckMock(endpoint = "/wrong-endpoint")
    public void fraudServiceErrorFallsBackToReview() {
        CreateUserRequest user1 = SessionStorage.getUser(1);
        CreateUserRequest user2 = SessionStorage.getUser(2);

        AccountSteps user1Steps = new AccountSteps(user1.getUsername(), user1.getPassword());
        AccountSteps user2Steps = new AccountSteps(user2.getUsername(), user2.getPassword());

        CreateAccountResponse account1 = user1Steps.createAccount();
        CreateAccountResponse account2 = user2Steps.createAccount();

        user1Steps.deposit(account1.getId(), 5000);

        TransferResponse response = user1Steps.transferWithFraudCheck(
                account1.getId(),
                account2.getId(),
                1000
        );

        softly.assertThat(response.getStatus()).isEqualTo(TransferStatus.MANUAL_REVIEW_REQUIRED.getValue());
        softly.assertThat(response.getRequiresManualReview()).isTrue();
    }
}