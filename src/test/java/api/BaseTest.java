package api;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class BaseTest {

    protected static final String BASE_URL = "https://stellarburgers.education-services.ru/api";
    protected String accessToken;

    @Before
    public void setUp() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.filters(
                new AllureRestAssured(),
                new RequestLoggingFilter(),
                new ResponseLoggingFilter()
        );
    }

    protected String registerUser(String email, String password, String name) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", email);
        requestBody.put("password", password);
        requestBody.put("name", name);

        Response response = given()
                .header("Content-type", "application/json")
                .body(requestBody)
                .when()
                .post("/auth/register");

        if (response.statusCode() == 200) {
            return response.jsonPath().getString("accessToken");
        }
        return null;
    }

    protected void deleteUser(String token) {
        if (token != null) {
            given()
                    .header("Authorization", token)
                    .when()
                    .delete("/auth/user");
        }
    }

    protected String generateRandomEmail() {
        return "testuser_" + System.currentTimeMillis() + "@yandex.ru";
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            deleteUser(accessToken);
        }
    }
}