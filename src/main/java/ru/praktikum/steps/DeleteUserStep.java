package ru.praktikum.steps;

import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

import static io.restassured.RestAssured.given;
import static ru.praktikum.config.ConfigClass.BASE_URL;

public class DeleteUserStep {

    @Step("User deletion")
    public ValidatableResponse deleteUser(String accessToken){
        return
                given()
                        .contentType(ContentType.JSON)
                        .baseUri(BASE_URL)
                        .header("Authorization",accessToken)
                        .when()
                        .delete("/api/auth/user")
                        .then();
    }
}