package ru.praktikum.steps;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import ru.praktikum.model.Order;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static ru.praktikum.Config.ConfigClass.BASE_URL;

public class OrderSteps {
    @Step("Получить список ингредиентов")
    public ValidatableResponse getIngredients() {
        return given()
                .contentType(JSON)
                .baseUri(BASE_URL)
                .when()
                .get("/api/ingredients")
                .then()
                .statusCode(200);
    }

    @Step("Создать заказ с авторизацией")
    public ValidatableResponse createOrderWithAuth(String accessToken, Order order) {
        return given()
                .contentType(JSON)
                .baseUri(BASE_URL)
                .header("Authorization", accessToken)
                .body(order)
                .when()
                .post("/api/orders")
                .then();
    }

    @Step("Создать заказ без авторизации")
    public ValidatableResponse createOrderWithoutAuth(Order order) {
        return given()
                .contentType(JSON)
                .baseUri(BASE_URL)
                .body(order)
                .when()
                .post("/api/orders")
                .then();
    }
    @Step("Получить список заказов с авторизацией")
    public ValidatableResponse getOrderListWithAuth(String accessToken){
        return
                given()
                        .contentType(JSON)
                        .header("Authorization", accessToken)
                        .baseUri(BASE_URL)
                        .when()
                        .get("/api/orders")
                        .then();
    }
    @Step("Ошибка получения заказов без авторизации")
    public ValidatableResponse getErrorForNoAuth(){
        return given()
                .contentType(JSON)
                .baseUri(BASE_URL)
                .when()
                .get("/api/orders")
                .then();
    }
}