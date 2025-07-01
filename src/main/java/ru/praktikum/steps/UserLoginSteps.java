package ru.praktikum.steps;

import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import ru.praktikum.model.Login;

import static io.restassured.RestAssured.given;
import static ru.praktikum.config.ConfigClass.BASE_URL;

public class UserLoginSteps {

    @Step("Логшин порльзователя")
    public ValidatableResponse loginUser(Login login){
        return
                given()
                        .contentType(ContentType.JSON)
                        .baseUri(BASE_URL)
                        .body(login)
                        .when()
                        .post("/api/auth/login/")
                        .then();
    }
}