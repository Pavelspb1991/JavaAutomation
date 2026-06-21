package api.requests.steps;

import api.models.*;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import io.restassured.specification.ResponseSpecification;
import java.util.Random;

public class AccountSteps {
    private final String username;
    private final String password;

    public AccountSteps(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public CreateAccountResponse createAccount() {
        return new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(username, password),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated()
        ).post();
    }

    public DepositMoneyResponse deposit(Long accountId, Number amount) {
        DepositMoneyRequest request = DepositMoneyRequest.builder()
                .id(accountId)
                .balance(amount)
                .build();
        return new ValidatedCrudRequester<DepositMoneyResponse>(
                RequestSpecs.authAsUser(username, password),
                Endpoint.DEPOSIT,
                ResponseSpecs.ok()
        ).post(request);
    }

    public void depositExpectingError(Long accountId, Number amount, ResponseSpecification errorSpec) {
        DepositMoneyRequest request = DepositMoneyRequest.builder()
                .id(accountId)
                .balance(amount)
                .build();
        new ValidatedCrudRequester<DepositMoneyResponse>(
                RequestSpecs.authAsUser(username, password),
                Endpoint.DEPOSIT,
                errorSpec
        ).postExpectingError(request);
    }

    public void depositWithoutAuth(Long accountId, Number amount) {
        DepositMoneyRequest request = DepositMoneyRequest.builder()
                .id(accountId)
                .balance(amount)
                .build();
        new ValidatedCrudRequester<DepositMoneyResponse>(
                RequestSpecs.unauthSpec(),
                Endpoint.DEPOSIT,
                ResponseSpecs.invalidToken()
        ).postExpectingError(request);
    }

    public void depositExpectingErrorWithRawBody(String rawBody, ResponseSpecification errorSpec) {
        new ValidatedCrudRequester<DepositMoneyResponse>(
                RequestSpecs.authAsUser(username, password),
                Endpoint.DEPOSIT,
                errorSpec
        ).postExpectingErrorWithRawBody(rawBody);
    }

    public TransferMoneyResponse transfer(Long senderId, Long receiverId, Number amount) {
        TransferMoneyRequest request = TransferMoneyRequest.builder()
                .senderAccountId(senderId)
                .receiverAccountId(receiverId)
                .amount(amount)
                .build();

        return new ValidatedCrudRequester<TransferMoneyResponse>(
                RequestSpecs.authAsUser(username, password),
                Endpoint.TRANSFER,
                ResponseSpecs.ok()
        ).post(request);
    }

    public void transferExpectingError(Long senderId, Long receiverId, Number amount,
                                       ResponseSpecification errorSpec) {
        TransferMoneyRequest request = TransferMoneyRequest.builder()
                .senderAccountId(senderId)
                .receiverAccountId(receiverId)
                .amount(amount)
                .build();

        new ValidatedCrudRequester<TransferMoneyResponse>(
                RequestSpecs.authAsUser(username, password),
                Endpoint.TRANSFER,
                errorSpec
        ).postExpectingError(request);
    }

    public void transferWithoutAuth(Long senderId, Long receiverId, Number amount) {
        TransferMoneyRequest request = TransferMoneyRequest.builder()
                .senderAccountId(senderId)
                .receiverAccountId(receiverId)
                .amount(amount)
                .build();

        new ValidatedCrudRequester<TransferMoneyResponse>(
                RequestSpecs.unauthSpec(),
                Endpoint.TRANSFER,
                ResponseSpecs.invalidToken()
        ).postExpectingError(request);
    }

    public void transferExpectingErrorWithRawBody(String rawBody, ResponseSpecification errorSpec) {
        new ValidatedCrudRequester<TransferMoneyResponse>(
                RequestSpecs.authAsUser(username, password),
                Endpoint.TRANSFER,
                errorSpec
        ).postExpectingErrorWithRawBody(rawBody);
    }

    public static Long generateNonExistentAccountId() {
        return new Random().nextLong(1000000000L, 9999999999L);
    }

    public static Number generateRandomTransferAmount() {
             return new Random().nextDouble(1.0, 10000.0);
         }
}