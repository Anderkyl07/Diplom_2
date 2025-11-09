package api;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class UserLoginTest extends BaseTest {

    private String testEmail;
    private String testPassword = "password123";
    private String testName = "Test User";

    @Before
    public void setUp() {
        super.setUp();
        testEmail = generateRandomEmail();
        accessToken = registerUser(testEmail, testPassword, testName);
    }

    @Test
    public void loginWithExistingUser_Success() {
        loginUserRequest(testEmail, testPassword)
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(testEmail))
                .body("user.name", equalTo(testName))
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue());
    }

    @Test
    public void loginWithInvalidCredentials_ReturnsError() {
        loginUserRequest("wrong@email.com", "wrongpassword")
                .then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    public void loginWithoutPassword_ReturnsError() {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", testEmail);

        given()
                .header("Content-type", "application/json")
                .body(requestBody)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Step("Авторизоваться с email: {email}, password: {password}")
    private Response loginUserRequest(String email, String password) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", email);
        requestBody.put("password", password);

        return given()
                .header("Content-type", "application/json")
                .body(requestBody)
                .when()
                .post("/auth/login");
    }

    @After
    public void tearDown() {
        super.tearDown();
    }
}