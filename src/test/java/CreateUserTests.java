import com.github.javafaker.Faker;
import io.qameta.allure.Description;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.praktikum.model.User;
import ru.praktikum.steps.DeleteUserStep;
import ru.praktikum.steps.UserCreationSteps;

import static org.hamcrest.CoreMatchers.equalTo;

public class CreateUserTests {
    private final UserCreationSteps userCreationSteps = new UserCreationSteps();
    private final Faker faker = new Faker();
    private DeleteUserStep deleteUserStep;
    private String accessToken;

    @Before
    public void setUp() {
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        deleteUserStep = new DeleteUserStep();
    }

    @Test
    @Description("Тест создания уникального пользователя")
    public void CreateUniqueUser() {
        String email = faker.internet().emailAddress();
        String password = faker.internet().password();
        String name = faker.name().fullName();
        User user = new User(email, password, name);
        ValidatableResponse authResponse = userCreationSteps.createUser(user)
                .statusCode(200)
                .body("success", equalTo(true));
        accessToken = authResponse.extract().path("accessToken");

    }

    @Test
    @Description("Тест создания не уникального пользователя, создали одного, потом такого же с теми же данными переменных, потом удалили\"")
    public void CreateNotUniqueUserTest() {
        String email = faker.internet().emailAddress();
        String password = faker.internet().password();
        String name = faker.name().fullName();
        User user = new User(email, password, name);
        ValidatableResponse authResponse = userCreationSteps.createUser(user)
                .statusCode(200)
                .body("success", equalTo(true));
        accessToken = authResponse.extract().path("accessToken");
        userCreationSteps.createUser(user)
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }

    @After
    @Description("Метод для удаления созданных пользователей")
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