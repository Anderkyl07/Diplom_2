package api;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class OrderCreationTest extends BaseTest {

    private String testEmail;
    private String testPassword = "password123";
    private String testName = "Test User";
    private List<String> validIngredients;

    @Before
    public void setUp() {
        super.setUp();
        testEmail = generateRandomEmail();
        accessToken = registerUser(testEmail, testPassword, testName);
        validIngredients = getValidIngredients();
    }

    @Test
    public void createOrderWithAuthorizationAndIngredients_Success() {
        String[] ingredients = {validIngredients.get(0), validIngredients.get(1)};
        createOrderRequest(accessToken, ingredients)
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("order.number", notNullValue())
                .body("name", notNullValue());
    }

    @Test
    public void createOrderWithoutAuthorizationWithIngredients_Success() {
        String[] ingredients = {validIngredients.get(0), validIngredients.get(1)};
        createOrderRequest(null, ingredients)
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("order.number", notNullValue())
                .body("name", notNullValue());
    }

    @Test
    public void createOrderWithoutIngredients_ReturnsError() {
        createOrderRequest(accessToken, new String[]{})
                .then()
                .statusCode(400)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    public void createOrderWithInvalidIngredientHash_ReturnsError() {
        String[] invalidIngredients = {"invalid_hash_1", "invalid_hash_2"};
        createOrderRequest(accessToken, invalidIngredients)
                .then()
                .statusCode(500);
    }

    @Test
    public void createOrderWithAuthorizationAndMultipleIngredients_Success() {
        String[] ingredients = {validIngredients.get(0), validIngredients.get(1), validIngredients.get(2)};
        createOrderRequest(accessToken, ingredients)
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("order.number", notNullValue());
    }

    @Step("Получить валидные ингредиенты")
    private List<String> getValidIngredients() {
        Response response = given()
                .when()
                .get("/ingredients");
        return response.jsonPath().getList("data._id");
    }

    @Step("Создать заказ с ингредиентами: {ingredients}")
    private Response createOrderRequest(String token, String[] ingredients) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("ingredients", Arrays.asList(ingredients));

        if (token != null) {
            return given()
                    .header("Content-type", "application/json")
                    .header("Authorization", token)
                    .body(requestBody)
                    .when()
                    .post("/orders");
        } else {
            return given()
                    .header("Content-type", "application/json")
                    .body(requestBody)
                    .when()
                    .post("/orders");
        }
    }

    @After
    public void tearDown() {
        super.tearDown();
    }
}