package ru.praktikum.steps;

import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import ru.praktikum.model.User;

import static io.restassured.RestAssured.given;
import static ru.praktikum.Config.ConfigClass.BASE_URL;

public class UserCreationSteps {

    @Step("Создание пользователя")
    public ValidatableResponse createUser(User user){
        return given()
                .contentType(ContentType.JSON)
                .baseUri(BASE_URL)
                .body(user)
                .when()
                .post("/api/auth/register")
                .then();
    }
}