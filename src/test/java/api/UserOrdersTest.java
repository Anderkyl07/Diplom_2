package api;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class UserOrdersTest extends BaseTest {

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

        // Создаем тестовый заказ для пользователя (используем только 2 ингредиента)
        String[] ingredients = {validIngredients.get(0), validIngredients.get(1)};
        createOrderRequest(accessToken, ingredients);
    }

    @Test
    public void getUserOrdersWithAuthorization_Success() {
        getUserOrdersRequest(accessToken)
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("orders", notNullValue())
                .body("total", greaterThanOrEqualTo(0))
                .body("totalToday", greaterThanOrEqualTo(0));
    }

    @Test
    public void getUserOrdersWithoutAuthorization_ReturnsError() {
        getUserOrdersRequest(null)
                .then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @Test
    public void getUserOrdersWithInvalidToken_ReturnsError() {
        getUserOrdersRequest("invalid_token")
                .then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @Step("Получить заказы пользователя")
    private Response getUserOrdersRequest(String token) {
        if (token != null) {
            return given()
                    .header("Authorization", token)
                    .when()
                    .get("/orders");
        } else {
            return given()
                    .when()
                    .get("/orders");
        }
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
        StringBuilder ingredientsJson = new StringBuilder("{\"ingredients\": [");
        for (int i = 0; i < ingredients.length; i++) {
            ingredientsJson.append("\"").append(ingredients[i]).append("\"");
            if (i < ingredients.length - 1) {
                ingredientsJson.append(", ");
            }
        }
        ingredientsJson.append("]}");

        return given()
                .header("Content-type", "application/json")
                .header("Authorization", token)
                .body(ingredientsJson.toString())
                .when()
                .post("/orders");
    }

    @After
    public void tearDown() {
        super.tearDown();
    }
}