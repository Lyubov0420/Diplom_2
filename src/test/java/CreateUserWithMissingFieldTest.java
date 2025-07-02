package ru.praktikum.tests;

import com.github.javafaker.Faker;
import io.qameta.allure.Description;
import io.restassured.response.ValidatableResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import ru.praktikum.model.User;
import ru.praktikum.steps.UserCreationSteps;

import static org.hamcrest.Matchers.equalTo;

@RunWith(Parameterized.class)
public class CreateUserWithMissingFieldTest {

    private static Faker faker = new Faker();
    private UserCreationSteps userCreationSteps = new UserCreationSteps();

    private User user;
    private String missingField;

    public CreateUserWithMissingFieldTest(User user, String missingField) {
        this.user = user;
        this.missingField = missingField;
    }

    @Parameterized.Parameters
    public static Object[][] data() {
        String email = faker.internet().emailAddress();
        String password = faker.internet().password();
        String name = faker.name().fullName();
        return new Object[][]{
                {new User("", password, name), "Email"},
                {new User(email, "", name), "Password"},
                {new User(email, password, ""), "Name"}
        };
    }

    @Test
    @Description("Создание пользователя без одного из полей с переметризацией что для любого из отсутствующих полей возвращается ошибка")
    public void createUserWithMissingField() {
        ValidatableResponse response = userCreationSteps.createUser(user);
        response.statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }
}