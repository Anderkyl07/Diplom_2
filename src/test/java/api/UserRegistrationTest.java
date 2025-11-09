package api;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class UserRegistrationTest extends BaseTest {

    @Test
    public void createUniqueUser_Success() {
        String email = generateRandomEmail();
        String password = "password123";
        String name = "Test User";

        createUserRequest(email, password, name)
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(email))
                .body("user.name", equalTo(name))
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue());
    }

    @Test
    public void createExistingUser_ReturnsError() {
        String email = generateRandomEmail();
        String password = "password123";
        String name = "Test User";

        // Создаем пользователя первый раз
        createUserRequest(email, password, name);

        // Пытаемся создать того же пользователя второй раз
        createUserRequest(email, password, name)
                .then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }

    @Test
    public void createUserWithoutRequiredField_ReturnsError() {
        String email = generateRandomEmail();
        String password = "password123";
        // Не передаем поле name

        String requestBody = String.format(
                "{\"email\": \"%s\", \"password\": \"%s\"}",
                email, password
        );

        createUserRequest(requestBody)
                .then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Step("Создать пользователя с email: {email}, password: {password}, name: {name}")
    private Response createUserRequest(String email, String password, String name) {
        String requestBody = String.format(
                "{\"email\": \"%s\", \"password\": \"%s\", \"name\": \"%s\"}",
                email, password, name
        );
        return createUserRequest(requestBody);
    }

    @Step("Создать пользователя с телом запроса: {requestBody}")
    private Response createUserRequest(String requestBody) {
        return given()
                .header("Content-type", "application/json")
                .body(requestBody)
                .when()
                .post("/auth/register");
    }
}