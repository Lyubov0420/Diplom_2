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

public class LoginUserTests {
    private final UserCreationSteps userCreationSteps = new UserCreationSteps();
    private final UserLoginSteps userLoginSteps = new UserLoginSteps();
    private DeleteUserStep deleteUserStep;
    private String accessToken;
    private final Faker faker = new Faker();
    private Login login;

    @Before
    public void setUp(){
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        deleteUserStep = new DeleteUserStep();
    }

    @Test
    @Description("Тест логина существующим пользователем, он создается, логинится и в After удаляется")
    public void LoginExistingUserTest() {
        String email = faker.internet().emailAddress();
        String password = faker.internet().password();
        String name = faker.name().fullName();
        User user = new User(email, password, name);
        userCreationSteps.createUser(user)
                .statusCode(200)
                .body("success", equalTo(true));
        login = new Login(email, password);
        ValidatableResponse authResponse = userLoginSteps.loginUser(login)
                .statusCode(200)
                .body("success", equalTo(true));
        accessToken = authResponse.extract().path("accessToken");
    }

    @Test
    @Description("Тест логина пользователя с неизвестными данными")
    public void LoginUnknownUser(){
        String email = faker.internet().emailAddress();
        String password = faker.internet().password();
        login = new Login (email, password);
        userLoginSteps.loginUser(login)
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }
    @After
    public void tearDown() {
        // Удаление пользователя
        if (accessToken != null && !accessToken.isEmpty()) {
            deleteUserStep.deleteUser(accessToken)
                    .statusCode(202)
                    .body("success", equalTo(true))
                    .body("message", equalTo("User successfully removed"));
        }
    }
}