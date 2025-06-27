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

public class CreateOrderTests {
    private final UserCreationSteps userCreationSteps = new UserCreationSteps();
    private final UserLoginSteps userLoginSteps = new UserLoginSteps();
    private DeleteUserStep deleteUserStep;
    private String accessToken;
    private final Faker faker = new Faker();
    private final OrderSteps orderSteps = new OrderSteps();

    @Before
    public void setUp(){
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        deleteUserStep = new DeleteUserStep();
    }

    @Test
    @Description("Тест создания заказа с авторизацией, он же проверяет и наличие ингридиентов так как без них нельзя создать заказ")
    public void createOrderWithAuthTest(){
        String email = faker.internet().emailAddress();
        String password = faker.internet().password();
        String name = faker.name().fullName();
        User user = new User(email, password, name);
        userCreationSteps.createUser(user);
        Login login = new Login(email, password);
        ValidatableResponse authResponse = userLoginSteps.loginUser(login);
        accessToken = authResponse.extract().path("accessToken");
        List<String> ids = orderSteps.getIngredients()
                .statusCode(200) // Добавляем проверку на успешное получение ингредиентов
                .body("success", equalTo(true)) // Проверка на наличие success
                .extract().path("data._id");
        Collections.shuffle(ids);
        Order order = new Order(List.of(ids.get(0),(ids.get(1))));
        orderSteps.createOrderWithAuth(accessToken, order)
                .statusCode(200)
                .body("success", equalTo(true));
    }
    @Test
    @Description("Тест создания заказа без авторизации и с ингридиентами. Заказ не должен создаваться без токена авторизации, так что это баг")
    public void createOrderWithoutAuthTest(){
        List<String> ids = orderSteps.getIngredients()
                .extract().path("data._id");
        Collections.shuffle(ids);
        Order order = new Order(List.of(ids.get(0),(ids.get(1))));
        orderSteps.createOrderWithoutAuth(order)
                .statusCode(401)
                .body("success",equalTo(false));
    }
    @Test
    @Description("Тест создания заказа без авторизации и без ингридиентов")
    public void createOrderWithoutIngredients(){
        Order order = new Order(List.of());
        orderSteps.createOrderWithoutAuth(order)
                .statusCode(400)
                .body("success",equalTo(false))
                .body("message",equalTo("Ingredient ids must be provided"));
    }

    @Test
    @Description("Тест создания заказа с неверным хэшем ингридиентов")
    public void createOrderWithInvalidIngredientsTest() {
        Order order = new Order(List.of("invalidHash"));
        orderSteps.createOrderWithoutAuth(order)
                .statusCode(500);
    }



    @After
    @Description("Метод для удаления созданных пользователей")
    public void tearDown() {
        // Удаление пользователя
        if (accessToken != null && !accessToken.isEmpty()) {
            deleteUserStep.deleteUser(accessToken)
                    .statusCode(202)
                    .body("success", equalTo(true));
        }
    }
}