package app.like;

import app.activitylog.event.ActivityLogEvent;
import app.exception.RecipeAlreadyLikedException;
import app.exception.UserCannotLikeOwnRecipeException;
import app.like.model.Like;
import app.like.repository.LikeRepository;
import app.like.service.LikeService;
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
public class LikeServiceIT {

    @Autowired
    private LikeService likeService;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private EventCaptureConfig eventCaptureConfig; // Captures the events.The @EventListener inside EventCaptureConfig will catch events published by UserService in a real database-backed test

    private WireMockServer wireMockServer;

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
    }

    @AfterEach
    void cleanUp() {
        recipeRepository.deleteAll();
        userRepository.deleteAll();
    }

    @AfterAll
    void stopWireMock() {
        wireMockServer.stop();
    }

    @Test
    public void testLike_shouldLikeRecipe() {
        User recipeCreator = aRandomWithoutId();
        User likeCreator = aRandomWithoutId();
        likeCreator.setUsername("likeCreator");
        likeCreator.setEmail("likeCreator@gmail.com");
        Recipe recipe = aRandomRecipeWithoutId();
        recipe.setCreatedBy(recipeCreator);

        userRepository.save(recipeCreator);
        User likerUser = userRepository.save(likeCreator);

        Recipe savedRecipe = recipeRepository.save(recipe);

        likeService.like(likerUser.getId(), savedRecipe.getId());

        List<ActivityLogEvent> capturedEvents = eventCaptureConfig.getCapturedEvents();
        ActivityLogEvent event = capturedEvents.get(0);
        String expectedMessage = "You have successfully liked recipe: " + recipe.getTitle();

        assertFalse(capturedEvents.isEmpty(), "No events were captured!");
        assertEquals(expectedMessage, event.getAction());

        eventCaptureConfig.clearCapturedEvents();
    }

    @Test
    void testLike_shouldNotLikeRecipe_whenUserIsRecipeCreator() {
        User recipeCreator = aRandomWithoutId();
        Recipe recipe = aRandomRecipeWithoutId();
        recipe.setCreatedBy(recipeCreator);

        User savedUser = userRepository.save(recipeCreator);

        Recipe savedRecipe = recipeRepository.save(recipe);

        assertThrows(UserCannotLikeOwnRecipeException.class, () -> likeService.like(savedUser.getId(), savedRecipe.getId()));
    }

    @Test
    void testLike_shouldNotLikeRecipe_whenRecipeIsAlreadyLiked() {
        User recipeCreator = aRandomWithoutId();
        User likeCreator = aRandomWithoutId();
        likeCreator.setUsername("likeCreator");
        likeCreator.setEmail("likeCreator@gmail.com");
        Recipe recipe = aRandomRecipeWithoutId();
        recipe.setCreatedBy(recipeCreator);

        userRepository.save(recipeCreator);
        User likerUser = userRepository.save(likeCreator);

        Recipe savedRecipe = recipeRepository.save(recipe);

        Like like = Like.builder()
                .user(likerUser)
                .recipe(savedRecipe)
                .build();

        likeRepository.save(like);

        assertThrows(RecipeAlreadyLikedException.class, () -> likeService.like(likeCreator.getId(), savedRecipe.getId()));
    }
}
