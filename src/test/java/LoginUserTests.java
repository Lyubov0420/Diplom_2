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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;

public class LoginUserTests {
    private final UserCreationSteps userCreationSteps = new UserCreationSteps();
    private final UserLoginSteps userLoginSteps = new UserLoginSteps();
    private final DeleteUserStep deleteUserStep = new DeleteUserStep();
    private final Faker faker = new Faker();

    private String accessToken;
    private User testUser;
    private Login validLogin;
    private String userEmail;
    private String userPassword;

    @Before
    public void setUp() {
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());

        // Создание тестового пользователя
        userEmail = faker.internet().emailAddress();
        userPassword = faker.internet().password();
        String name = faker.name().fullName();

        testUser = new User(userEmail, userPassword, name);
        userCreationSteps.createUser(testUser)
                .statusCode(200)
                .body("success", equalTo(true));

        // Подготовка валидных данных для логина
        validLogin = new Login(userEmail, userPassword);
    }

    @Test
    @Description("Тест успешного логина существующим пользователем")
    public void loginExistingUserTest() {
        ValidatableResponse authResponse = userLoginSteps.loginUser(validLogin)
                .statusCode(200)
                .body("success", equalTo(true));

        accessToken = authResponse.extract().path("accessToken");
        assertNotNull("Access token должен быть получен", accessToken);
    }

    @Test
    @Description("Тест логина с неверным email")
    public void loginWithWrongEmailTest() {
        Login wrongEmailLogin = new Login("wrong_" + userEmail, userPassword);
        userLoginSteps.loginUser(wrongEmailLogin)
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @Description("Тест логина с неверным паролем")
    public void loginWithWrongPasswordTest() {
        Login wrongPasswordLogin = new Login(userEmail, "wrong_" + userPassword);
        userLoginSteps.loginUser(wrongPasswordLogin)
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @After
    @Description("Удаление тестового пользователя")
    public void tearDown() {
        if (accessToken != null && !accessToken.isEmpty()) {
            deleteUserStep.deleteUser(accessToken)
                    .statusCode(202)
                    .body("success", equalTo(true))
                    .body("message", equalTo("User successfully removed"));
        }
    }
}