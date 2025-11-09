package api;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

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

        createUserRequest(email, password, name);
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

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", email);
        requestBody.put("password", password);

        createUserRequest(requestBody)
                .then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Step("Создать пользователя с email: {email}, password: {password}, name: {name}")
    private Response createUserRequest(String email, String password, String name) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", email);
        requestBody.put("password", password);
        requestBody.put("name", name);
        return createUserRequest(requestBody);
    }

    @Step("Создать пользователя с телом запроса: {requestBody}")
    private Response createUserRequest(Map<String, Object> requestBody) {
        return given()
                .header("Content-type", "application/json")
                .body(requestBody)
                .when()
                .post("/auth/register");
    }
}