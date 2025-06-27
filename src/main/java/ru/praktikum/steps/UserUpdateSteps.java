package ru.praktikum.steps;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import ru.praktikum.model.User;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static ru.praktikum.Config.ConfigClass.BASE_URL;

public class UserUpdateSteps {

    @Step("Обновление данных пользователя с авторизацией")
    public ValidatableResponse updateUserWithAuth(String accessToken, User updatedUser){
        return
                given()
                        .contentType(JSON)
                        .baseUri(BASE_URL)
                        .header("Authorization", accessToken)
                        .body(updatedUser)
                        .when()
                        .patch("/api/auth/user")
                        .then();
    }
    @Step("Обновление данных пользователя без авторизации")
    public ValidatableResponse updateUserWithoutAuth(User updatedUser) {
        return given()
                .contentType(JSON)
                .baseUri(BASE_URL)
                .body(updatedUser)
                .when()
                .patch("/api/auth/user")
                .then();
    }
}