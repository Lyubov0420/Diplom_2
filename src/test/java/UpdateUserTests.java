import com.github.javafaker.Faker;
import io.qameta.allure.Description;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.praktikum.model.Login;
import ru.praktikum.model.User;
import ru.praktikum.steps.DeleteUserStep;
import ru.praktikum.steps.UserCreationSteps;
import ru.praktikum.steps.UserLoginSteps;
import ru.praktikum.steps.UserUpdateSteps;

import static org.hamcrest.CoreMatchers.equalTo;

public class UpdateUserTests {
    private final UserCreationSteps userCreationSteps = new UserCreationSteps();
    private final UserLoginSteps userLoginSteps = new UserLoginSteps();
    private final UserUpdateSteps userUpdateSteps = new UserUpdateSteps();
    private final DeleteUserStep deleteUserStep = new DeleteUserStep();
    private final Faker faker = new Faker();

    private User user;
    private String accessToken;

    @Before
    public void setUp() {
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());

        // Создание тестового пользователя перед каждым тестом
        String email = faker.internet().emailAddress();
        String password = faker.internet().password();
        String name = faker.name().fullName();
        user = new User(email, password, name);
        userCreationSteps.createUser(user);
    }

    @Test
    @Description("Обновление данных пользователя с авторизацией")
    public void updateUserWithAuthTest() {
        // Авторизация пользователя
        Login login = new Login(user.getEmail(), user.getPassword());
        ValidatableResponse authResponse = userLoginSteps.loginUser(login);
        accessToken = authResponse.extract().path("accessToken");

        // Подготовка новых данных
        String newName = faker.name().fullName();
        String newEmail = faker.internet().emailAddress();
        String newPassword = faker.internet().password();
        User updatedUser = new User(newEmail, newPassword, newName);

        // Обновление данных и проверки
        userUpdateSteps.updateUserWithAuth(accessToken, updatedUser)
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(newEmail))
                .body("user.name", equalTo(newName));
    }

    @Test
    @Description("Попытка обновления данных пользователя без авторизации")
    public void updateUserWithoutAuthTest() {
        // Подготовка новых данных
        String newName = faker.name().fullName();
        String newEmail = faker.internet().emailAddress();
        String newPassword = faker.internet().password();
        User updatedUser = new User(newEmail, newPassword, newName);

        // Попытка обновления без авторизации и проверки
        userUpdateSteps.updateUserWithoutAuth(updatedUser)
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @After
    @Description("Удаление созданных пользователей после тестов")
    public void tearDown() {
        if (accessToken != null && !accessToken.isEmpty()) {
            deleteUserStep.deleteUser(accessToken)
                    .statusCode(202)
                    .body("success", equalTo(true))
                    .body("message", equalTo("User successfully removed"));
        }
    }
}