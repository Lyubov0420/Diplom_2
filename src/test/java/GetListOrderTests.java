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
import static org.hamcrest.CoreMatchers.notNullValue;

public class GetListOrderTests {
    private final UserCreationSteps userCreationSteps = new UserCreationSteps();
    private final UserLoginSteps userLoginSteps = new UserLoginSteps();
    private final DeleteUserStep deleteUserStep = new DeleteUserStep();
    private final Faker faker = new Faker();
    private final OrderSteps orderSteps = new OrderSteps();

    private String accessToken;
    private User user;
    private Order createdOrder;

    @Before
    public void setUp() {
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());

        // Создание и регистрация пользователя
        String email = faker.internet().emailAddress();
        String password = faker.internet().password();
        String name = faker.name().fullName();
        user = new User(email, password, name);
        userCreationSteps.createUser(user);

        // Авторизация пользователя
        Login login = new Login(email, password);
        ValidatableResponse authResponse = userLoginSteps.loginUser(login);
        accessToken = authResponse.extract().path("accessToken");

        // Создание тестового заказа
        List<String> ids = orderSteps.getIngredients()
                .statusCode(200)
                .body("success", equalTo(true))
                .extract().path("data._id");
        Collections.shuffle(ids);
        createdOrder = new Order(List.of(ids.get(0), ids.get(1)));
        orderSteps.createOrderWithAuth(accessToken, createdOrder);
    }

    @Test
    @Description("Тест получения заказов пользователя с авторизацией")
    public void getOrdersListWithAuth() {
        orderSteps.getOrderListWithAuth(accessToken)
                .statusCode(200)
                .body("success", equalTo(true))
                .body("orders", notNullValue())
                .body("orders[0].ingredients", notNullValue());
    }

    @Test
    @Description("Тест получения заказов пользователя без авторизации")
    public void getOrdersListWithoutAuth() {
        orderSteps.getErrorForNoAuth()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @After
    @Description("Удаление созданного пользователя")
    public void tearDown() {
        if (accessToken != null && !accessToken.isEmpty()) {
            deleteUserStep.deleteUser(accessToken)
                    .statusCode(202)
                    .body("success", equalTo(true))
                    .body("message", equalTo("User successfully removed"));
        }
    }
}