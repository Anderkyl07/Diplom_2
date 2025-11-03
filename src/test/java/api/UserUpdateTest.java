package api;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class UserUpdateTest extends BaseTest {

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
    public void updateUserEmailWithAuthorization_Success() {
        String newEmail = "updated_" + generateRandomEmail();

        updateUserRequest(accessToken, newEmail, testName)
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(newEmail))
                .body("user.name", equalTo(testName));
    }

    @Test
    public void updateUserNameWithAuthorization_Success() {
        String newName = "Updated User Name";

        updateUserRequest(accessToken, testEmail, newName)
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(testEmail))
                .body("user.name", equalTo(newName));
    }

    @Test
    public void updateUserPasswordWithAuthorization_Success() {
        String newPassword = "newpassword123";

        String requestBody = String.format(
                "{\"email\": \"%s\", \"password\": \"%s\", \"name\": \"%s\"}",
                testEmail, newPassword, testName
        );

        updateUserRequest(accessToken, requestBody)
                .then()
                .statusCode(200)
                .body("success", equalTo(true));
    }

    @Test
    public void updateUserWithoutAuthorization_ReturnsError() {
        String newEmail = "updated_" + generateRandomEmail();

        updateUserRequest(null, newEmail, testName)
                .then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @Test
    public void updateUserWithExistingEmail_ReturnsError() {
        // Создаем второго пользователя
        String secondUserEmail = generateRandomEmail();
        String secondUserToken = registerUser(secondUserEmail, "password456", "Second User");

        // Пытаемся обновить email первого пользователя на email второго
        updateUserRequest(accessToken, secondUserEmail, testName)
                .then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("User with such email already exists"));

        // Удаляем второго пользователя
        deleteUser(secondUserToken);
    }

    @Step("Обновить данные пользователя с email: {email}, name: {name}")
    private Response updateUserRequest(String token, String email, String name) {
        String requestBody = String.format(
                "{\"email\": \"%s\", \"name\": \"%s\"}",
                email, name
        );
        return updateUserRequest(token, requestBody);
    }

    @Step("Обновить данные пользователя с телом запроса: {requestBody}")
    private Response updateUserRequest(String token, String requestBody) {
        if (token != null) {
            return given()
                    .header("Content-type", "application/json")
                    .header("Authorization", token)
                    .body(requestBody)
                    .when()
                    .patch("/auth/user");
        } else {
            return given()
                    .header("Content-type", "application/json")
                    .body(requestBody)
                    .when()
                    .patch("/auth/user");
        }
    }

    @After
    public void tearDown() {
        super.tearDown();
    }
}