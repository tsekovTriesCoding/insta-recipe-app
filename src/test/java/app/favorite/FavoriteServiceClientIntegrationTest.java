package app.favorite;

import app.config.WebClientConfig;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Disabled
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)  // Use this to avoid reinitializing MockWebServer for each test
public class FavoriteServiceClientIntegrationTest {

    private static MockWebServer mockWebServer;

    @Autowired
    private FavoriteServiceClient favoriteServiceClient;

    @Autowired
    private WebClientConfig webClientConfig;

    @BeforeAll
    void setUp() throws IOException {
        // Create and start the MockWebServer
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        webClientConfig.setUrl(mockWebServer.url("/").toString());
    }

    @AfterAll
    void tearDown() throws IOException {
        // Shut down the MockWebServer after all tests are done
        mockWebServer.shutdown();
    }

    @Test
    void testGetFavoriteRecipeIds() {
        // Prepare the mock response from the MockWebServer
        String mockResponseJson = "[{'recipeId': 'abcde'}]";

        // Enqueue the mock response (the response that the FavoriteServiceClient will receive)
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)  // Simulate a successful response
                .setBody(mockResponseJson)  // Set the mock response body
                .addHeader("Content-Type", "application/json"));
        // Call the method to test

        List<UUID> favoriteRecipeIds = favoriteServiceClient.getFavoriteRecipeIds(UUID.randomUUID());

        // Verify the result
        assertNotNull(favoriteRecipeIds);
        assertEquals(1, favoriteRecipeIds.size());
        // Check if the UUID is correctly parsed from the mock response
        assertEquals(UUID.fromString("abcde"), favoriteRecipeIds.get(0));
    }
}
