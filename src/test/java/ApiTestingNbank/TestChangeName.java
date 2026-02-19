package ApiTestingNbank;
import generators.RandomData;
import models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import requests.GetCustomerProfileRequester;
import requests.UpdateCustomerProfileRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;
import java.util.stream.Stream;


public class TestChangeNamee extends BaseTest {

    // Метод, который возвращает стрим валидных значений
    static Stream<String> validNames() {
        return Stream.of("A B",
                "Ab Cd",
                "John Doe",
                "Abcdefghijaaaa Klmnopqrstaaaaaaaa");
    }

    // Метод, который возвращает стрим невалидных значений
    static Stream<String> invalidNames() {
        return Stream.of("A",
                "AbCd",
                "John1 Doe",
                "John Doe1",
                "John1 Doe Jack",
                "Привет",
                "");
    }

    // Параметризованный тест, проверка возможности изменения имени пользователя - валидные данные
    //Источник входных данных - @MethodSource("validNames")
    @ParameterizedTest
    @MethodSource("validNames")
    public void userCanChangeNameCheckValidData(String newName) {
        //получаем данные пользователя с метода createUser, который в BeforeAll создает его перед тестами (класс BaseTest)
        String username = createdUserRequest.getUsername();
        String password = createdUserRequest.getPassword();
        //Создаем тело запроса для метода put
        UpdateCustomerProfileRequest request = UpdateCustomerProfileRequest.builder().name(newName).build();
        //Отправляем PUT запрос и записываем результат в updateCustomerProfileResponse
        UpdateCustomerProfileResponse updateCustomerProfileResponse = new UpdateCustomerProfileRequester(
                RequestSpecs.userSpec(username,password), ResponseSpecs.ok()).put(request)
                .extract().as(UpdateCustomerProfileResponse.class);
        // Проверяем значение поля name
        softly.assertThat(request.getName())
                .isEqualTo(updateCustomerProfileResponse.getCustomer().getName());
        //Отправляем GET запрос и записываем результат в getResponse
        CustomerData getResponse = new
                GetCustomerProfileRequester(RequestSpecs.userSpec(username,password), ResponseSpecs.ok())
                .get().extract().as(CustomerData.class);
        // Проверяем, что значение поля name изменилось
        softly.assertThat(getResponse.getName())
                .isEqualTo(newName);
        }




    //Параметризованный тест на проверку невозможности изменения имени пользователя при невалидных данных.
    //Источник входных данных - @MethodSource("invalidNames")
    @ParameterizedTest
    @MethodSource("invalidNames")
    public void userCantChangeNameWithInvalidData(String invalidName) {
        //получаем данные пользователя с метода createUser, который в BeforeAll создает его перед тестами (класс BaseTest)
        String username = createdUserRequest.getUsername();
        String password = createdUserRequest.getPassword();
        //Создаем тело запроса для метода put
        UpdateCustomerProfileRequest badRequest = UpdateCustomerProfileRequest.builder().name(invalidName).build();
        //Отправляем PUT запрос и записываем результат в errorMessage типа String
        String errorMessage = new UpdateCustomerProfileRequester(
                RequestSpecs.userSpec(username,password), ResponseSpecs.invalidDataProvided()).put(badRequest)
                .extract().asString();
        //Проверяем текст сообщения об ошибке
        softly.assertThat(errorMessage.equals(ErrorMessages.INVALID_NAME));
        //Отправляем GET запрос и записываем результат в getResponse
        CustomerData getResponse = new
                GetCustomerProfileRequester(RequestSpecs.userSpec(username,password), ResponseSpecs.ok())
                .get().extract().as(CustomerData.class);
        // Проверяем, что значение поля name не изменилось
        softly.assertThat(getResponse.getName())
                .isNotEqualTo(invalidName);


    }
    //Тест на проверку невозможности изменения имени пользователя без авторизации
    @Test
    public void userCantChangeNameWithoutToken() {
        //получаем данные пользователя с метода createUser, который в BeforeAll создает его перед тестами (класс BaseTest)
        String username = createdUserRequest.getUsername();
        String password = createdUserRequest.getPassword();
        //Создаем тело запроса для метода put
        UpdateCustomerProfileRequest badRequest = UpdateCustomerProfileRequest
                .builder().name(RandomData.validUpdateName()).build();
        //Отправляем PUT запрос без записывания результата в переменную (с проверкой на 401 статус-код)
        new UpdateCustomerProfileRequester(
                RequestSpecs.unauthSpec(), ResponseSpecs.invalidToken()).put(badRequest)
                .extract().asString();

        //Отправляем GET запрос и записываем результат в getResponse
        CustomerData getResponse = new
                GetCustomerProfileRequester(RequestSpecs.userSpec(username,password), ResponseSpecs.ok())
                .get().extract().as(CustomerData.class);
        // Проверяем, что значение поля name не изменилось
        softly.assertThat(getResponse.getName())
                .isNotEqualTo(badRequest.getName());







    }
    }




