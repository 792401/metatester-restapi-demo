import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static io.restassured.RestAssured.given;

@Execution(ExecutionMode.CONCURRENT)
public class UsersTest {

    private static final String BEARER_TOKEN = "Bearer 123";

    @BeforeAll
    public static void beforeAll(){
        RestAssured.baseURI = "http://localhost:8089";
    }

    @Test
    public void testGetUsers() {
        Response response = given()
                .header("Authorization", BEARER_TOKEN)
                .queryParam("page", 1)
                .queryParam("limit", 50)
                .queryParam("role", "user")
                .when()
                .get("/api/v1/users");

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertFalse(response.jsonPath().getList("data").isEmpty());
        Assertions.assertEquals(3, response.jsonPath().getList("data").size());
        Assertions.assertTrue(response.jsonPath().getInt("pagination.total") > 0);
    }

    @Test
    public void testGetUser() {
        int userId = 1001;
        
        Response response = given()
                .when()
                .get("/api/v1/users/" + userId);

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(userId, response.jsonPath().getInt("id"));
        Assertions.assertNotNull(response.jsonPath().getString("username"));
        Assertions.assertNotNull(response.jsonPath().getString("email"));
        Assertions.assertNotNull(response.jsonPath().getString("profile.firstName"));
        Assertions.assertNotNull(response.jsonPath().getString("profile.lastName"));
    }

    @Test
    public void testUserSubscriptionData() {
        int userId = 1001;
        
        Response response = given()
                .when()
                .get("/api/v1/users/" + userId);

        // Test subscription structure
        Assertions.assertEquals("premium", response.jsonPath().getString("subscription.plan"));
        Assertions.assertEquals("active", response.jsonPath().getString("subscription.status"));
        Assertions.assertNotNull(response.jsonPath().getString("subscription.expiresAt"));
        Assertions.assertFalse(response.jsonPath().getList("subscription.features").isEmpty());
        
        // Test that features array contains expected values
        Assertions.assertTrue(response.jsonPath().getList("subscription.features").contains("advanced_analytics"));
    }

    @Test
    public void testUserPreferencesWithNullValues() {
        int userId = 1001;
        
        Response response = given()
                .when()
                .get("/api/v1/users/" + userId);

        // Test preferences structure including null handling
        Assertions.assertEquals(true, response.jsonPath().getBoolean("preferences.notifications.email"));
        Assertions.assertEquals(false, response.jsonPath().getBoolean("preferences.notifications.push"));
        
        // Test null value handling - sms is explicitly null in mock data
        Object smsNotification = response.jsonPath().get("preferences.notifications.sms");
        Assertions.assertNull(smsNotification);
        
        Assertions.assertEquals("dark", response.jsonPath().getString("preferences.theme"));
        Assertions.assertEquals("en", response.jsonPath().getString("preferences.language"));
    }

    @Test
    public void testUserMetadata() {
        int userId = 1001;
        
        Response response = given()
                .when()
                .get("/api/v1/users/" + userId);

        // Test metadata fields
        Assertions.assertNotNull(response.jsonPath().getString("metadata.createdAt"));
        Assertions.assertNotNull(response.jsonPath().getString("metadata.lastLoginAt"));
        Assertions.assertTrue(response.jsonPath().getInt("metadata.loginCount") > 0);
        Assertions.assertEquals(true, response.jsonPath().getBoolean("metadata.isVerified"));
        
        // Test timestamp format
        String createdAt = response.jsonPath().getString("metadata.createdAt");
        Assertions.assertTrue(createdAt.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z"));
    }

    @Test
    public void testUsersListFiltering() {
        Response response = given()
                .header("Authorization", BEARER_TOKEN)
                .queryParam("page", 1)
                .queryParam("limit", 50)
                .queryParam("role", "admin")
                .when()
                .get("/api/v1/users");

        // Should still return all users since our mock doesn't filter, but structure should be correct
        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertFalse(response.jsonPath().getList("data").isEmpty());
        
        // Test that at least one admin exists in the data
        boolean hasAdmin = response.jsonPath().getList("data").stream()
                .anyMatch(user -> "admin".equals(((java.util.Map<?, ?>) user).get("role")));
        Assertions.assertTrue(hasAdmin);
    }
}
