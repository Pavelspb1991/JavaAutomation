package generators;
import org.apache.commons.lang3.RandomStringUtils;

public class RandomData {
    private RandomData() {}

    //Метод для генерации случайного логина
    public static String getUsername() {
        return RandomStringUtils.randomAlphabetic(9);
    }

    //Метод для генерации случайного пароля
    public static String getPassword() {
        return RandomStringUtils.randomAlphabetic(3).toUpperCase() +
                RandomStringUtils.randomAlphabetic(5).toLowerCase() +
                RandomStringUtils.randomNumeric(3) + "%" ;
    }

    //Метод для генерации случайного валидного имени
    public static String validUpdateName() {
        return RandomStringUtils.randomAlphabetic(2) + " " + RandomStringUtils.randomAlphabetic(2);
    }
}