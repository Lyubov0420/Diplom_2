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
import ru.praktikum.model.Order;
import ru.praktikum.model.User;
import ru.praktikum.steps.DeleteUserStep;
import ru.praktikum.steps.OrderSteps;
import ru.praktikum.steps.UserCreationSteps;
import ru.praktikum.steps.UserLoginSteps;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;

public class CreateOrderTests {
    private final UserCreationSteps userCreationSteps = new UserCreationSteps();
    private final UserLoginSteps userLoginSteps = new UserLoginSteps();
    private final DeleteUserStep deleteUserStep = new DeleteUserStep();
    private final Faker faker = new Faker();
    private final OrderSteps orderSteps = new OrderSteps();

    private String accessToken;
    private User user;

    @Before
    public void setUp() {
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());

        // Создание тестового пользователя
        String email = faker.internet().emailAddress();
        String password = faker.internet().password();
        String name = faker.name().fullName();

        user = new User(email, password, name);
        userCreationSteps.createUser(user)
                .statusCode(200)
                .body("success", equalTo(true));

        // Авторизация пользователя
        Login login = new Login(email, password);
        ValidatableResponse authResponse = userLoginSteps.loginUser(login)
                .statusCode(200)
                .body("success", equalTo(true));

        accessToken = authResponse.extract().path("accessToken");
        assertNotNull("Access token должен быть получен", accessToken);
    }

    @Test
    @Description("Тест создания заказа с авторизацией")
    public void createOrderWithAuthTest() {
        List<String> ids = orderSteps.getIngredients()
                .statusCode(200)
                .body("success", equalTo(true))
                .extract().path("data._id");

        Collections.shuffle(ids);
        Order order = new Order(List.of(ids.get(0), ids.get(1)));

        orderSteps.createOrderWithAuth(accessToken, order)
                .statusCode(200)
                .body("success", equalTo(true));
    }

    @Test
    @Description("Тест создания заказа без авторизации")
    public void createOrderWithoutAuthTest() {
        List<String> ids = orderSteps.getIngredients()
                .statusCode(200)
                .body("success", equalTo(true))
                .extract().path("data._id");

        Collections.shuffle(ids);
        Order order = new Order(List.of(ids.get(0), ids.get(1)));

        orderSteps.createOrderWithoutAuth(order)
                .statusCode(401)  // Ожидаем 401 Unauthorized
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @Test
    @Description("Тест создания заказа без ингредиентов")
    public void createOrderWithoutIngredients() {
        Order order = new Order(List.of());
        orderSteps.createOrderWithoutAuth(order)
                .statusCode(400)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @Description("Тест создания заказа с неверным хэшем ингредиентов")
    public void createOrderWithInvalidIngredientsTest() {
        Order order = new Order(List.of("invalidHash"));
        orderSteps.createOrderWithoutAuth(order)
                .statusCode(500);
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