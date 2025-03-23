package app.favorite;

import app.activitylog.event.ActivityLogEvent;
import app.exception.AlreadyFavoritedException;
import app.exception.FavoriteNotFoundException;
import app.favorite.model.Favorite;
import app.favorite.repository.FavoriteRepository;
import app.favorite.service.FavoriteService;
import app.recipe.model.Recipe;
import app.recipe.repository.RecipeRepository;
import app.recipe.service.RecipeService;
import app.user.model.User;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.event.EventListener;

import java.util.ArrayList;
import java.util.List;

import static app.TestBuilder.aRandomRecipeWithoutId;
import static app.TestBuilder.aRandomWithoutId;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FavoriteServiceIT {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private EventCaptureConfig eventCaptureConfig; // Captures the events.The @EventListener inside EventCaptureConfig will catch events published by UserService in a real database-backed test

    private WireMockServer wireMockServer;

    private User savedUser;
    private Recipe savedRecipe;

    // Do not use @MockBean ApplicationEventPublisher in integration tests.
    //It replaces the real publisher with a mock, preventing real event propagation.
    //Mocking ApplicationEventPublisher in integration tests can bypass the event-driven nature of Spring, which isn't ideal for testing how your application reacts to events.
    @TestConfiguration
    static class EventCaptureConfig {

        private final List<ActivityLogEvent> capturedEvents = new ArrayList<>();

        @EventListener
        public void onActivityLogEvent(ActivityLogEvent event) {
            capturedEvents.add(event);
        }

        public void clearCapturedEvents() {
            capturedEvents.clear();
        }

        public List<ActivityLogEvent> getCapturedEvents() {
            return capturedEvents;
        }
    }

    @BeforeAll
    void startWireMock() {
        wireMockServer = new WireMockServer(8081); // Same port as the real service
        wireMockServer.start();

        wireMockServer.stubFor(post(urlEqualTo("/api/v1/activity-log"))
                .willReturn(aResponse().withStatus(200))); // Mock successful response

        User user = aRandomWithoutId();

        Recipe recipe = aRandomRecipeWithoutId();
        recipe.setCreatedBy(user);

        savedUser = userRepository.save(user);
        savedRecipe = recipeRepository.save(recipe);
    }

    @AfterEach
    void cleanUp() {
        favoriteRepository.deleteAll();
    }

    @AfterAll
    void stopWireMock() {
        wireMockServer.stop();

        recipeRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testAddRecipeToFavorites_shouldAddFavorite() {
        favoriteService.addRecipeToFavorites(savedUser.getId(), savedRecipe.getId());

        List<ActivityLogEvent> capturedEvents = eventCaptureConfig.getCapturedEvents();
        ActivityLogEvent event = capturedEvents.get(0);
        String expectedMessage = "You have successfully added recipe: " + savedRecipe.getTitle() + " to your favorites";

        assertFalse(capturedEvents.isEmpty(), "No events were captured!");
        assertEquals(expectedMessage, event.getAction());

        eventCaptureConfig.clearCapturedEvents();
    }

    @Test
    void testAddRecipeToFavorites_shouldNotAddFavorite_andThrowException() {
        Favorite favorite = Favorite.builder()
                .user(savedUser)
                .recipe(savedRecipe)
                .build();

        favoriteRepository.save(favorite);

        assertThrows(AlreadyFavoritedException.class, () -> favoriteService.addRecipeToFavorites(savedUser.getId(), savedRecipe.getId()));
    }

    @Test
    void testRemoveRecipeFromFavorites_shouldRemoveFavorite() {
        Favorite favorite = Favorite.builder()
                .user(savedUser)
                .recipe(savedRecipe)
                .build();

        favoriteRepository.save(favorite);

        favoriteService.removeRecipeFromFavorites(savedUser.getId(), savedRecipe.getId());

        List<ActivityLogEvent> capturedEvents = eventCaptureConfig.getCapturedEvents();
        ActivityLogEvent event = capturedEvents.get(0);
        String expectedMessage = "You have successfully removed recipe: " + savedRecipe.getTitle() + " from your favorites";

        assertFalse(capturedEvents.isEmpty(), "No events were captured!");
        assertEquals(expectedMessage, event.getAction());

        eventCaptureConfig.clearCapturedEvents();
    }

    @Test
    void testRemoveRecipeFromFavorites_shouldNotRemoveFavorite_andThrowException() {
        assertThrows(FavoriteNotFoundException.class, () -> favoriteService.removeRecipeFromFavorites(savedUser.getId(), savedRecipe.getId()));
    }

    @Test
    void testGetUserFavoriteRecipes_shouldGetFavorites() {
        Favorite favorite = Favorite.builder()
                .user(savedUser)
                .recipe(savedRecipe)
                .build();

        favoriteRepository.save(favorite);

        List<Recipe> userFavoriteRecipes = favoriteService.getUserFavoriteRecipes(savedUser.getId());

        assertEquals(1, userFavoriteRecipes.size());
    }
}
