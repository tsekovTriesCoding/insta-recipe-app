package app.web;

import app.activitylog.event.ActivityLogEvent;
import app.comment.repository.CommentRepository;
import app.config.EventCaptureConfig;
import app.recipe.model.Recipe;
import app.recipe.repository.RecipeRepository;
import app.security.CustomUserDetails;
import app.user.model.Role;
import app.user.model.User;
import app.user.repository.UserRepository;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static app.TestBuilder.aRandomRecipeWithoutId;
import static app.TestBuilder.aRandomWithoutId;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = EventCaptureConfig.class)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CommentControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventCaptureConfig eventCaptureConfig;

    private WireMockServer wireMockServer;

    private UUID recipeId;

    @BeforeAll
    void startWireMock() {
        wireMockServer = new WireMockServer(8081); // Same port as the real service
        wireMockServer.start();

        wireMockServer.stubFor(WireMock.post(urlEqualTo("/api/v1/activity-log"))
                .willReturn(aResponse().withStatus(201))); // Mock successful response
    }

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll(); // Clean database before each test
        recipeId = UUID.randomUUID();  // Sample recipe ID
    }

    @AfterAll
    void stopWireMock() {
        wireMockServer.stop();
        commentRepository.deleteAll();
        recipeRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testAddComment_shouldFailWhenContentIsMissing() throws Exception {
        mockMvc.perform(post("/comments/add/" + recipeId)
                        .with(csrf()) // Required for security
                        .with(user("testUser")) // Simulating a logged-in user
                        .param("content", ""))
                .andExpect(status().is3xxRedirection()) // Redirects back to recipe
                .andExpect(redirectedUrl("/recipes/" + recipeId))
                .andExpect(flash().attribute("error", "Please enter a comment"));

        assertFalse(commentRepository.existsByRecipe_Id(recipeId));
    }

    @Test
    void testAddComment_shouldFailWhenContentIsTooLong() throws Exception {
        String longContent = "a".repeat(501); // 501 characters (exceeds limit)

        mockMvc.perform(post("/comments/add/" + recipeId)
                        .with(csrf())
                        .with(user("testUser"))
                        .param("content", longContent))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recipes/" + recipeId))
                .andExpect(flash().attribute("error", "Content is too long"));

        assertFalse(commentRepository.existsByRecipe_Id(recipeId));
    }

    @Test
    void testAddComment_shouldFailWhenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(post("/comments/add/" + recipeId)
                        .with(csrf())
                        .param("content", "Nice recipe!"))
                .andExpect(status().is3xxRedirection()) // Redirects to login
                .andExpect(redirectedUrlPattern("**/login")); // Ensure it redirects to login

        assertFalse(commentRepository.existsByRecipe_Id(recipeId));
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public ApplicationEventPublisher applicationEventPublisher() {
            return Mockito.mock(ApplicationEventPublisher.class);
        }
    }

    @Test
    void testAddComment_shouldAddCommentWhenValidRequest() throws Exception {
        User user = aRandomWithoutId();
        User savedUser = userRepository.save(user);

        CustomUserDetails principal = new CustomUserDetails(savedUser.getId(), "user", "pass", Role.USER, true);

        Recipe recipe = aRandomRecipeWithoutId();
        recipe.setCreatedBy(user);
        Recipe savedRecipe = recipeRepository.save(recipe);

        mockMvc.perform(post("/comments/add/" + savedRecipe.getId())
                        .with(csrf())
                        .with(user(principal))
                        .param("content", "This is a test comment"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recipes/" + savedRecipe.getId()));

        List<ActivityLogEvent> capturedEvents = eventCaptureConfig.getCapturedEvents();
        ActivityLogEvent event = capturedEvents.get(0);
        String expectedMessage = "You have successfully commented [%s], on recipe: %s".formatted("This is a test comment", recipe.getTitle());


        assertTrue(commentRepository.existsByRecipe_Id(savedRecipe.getId()));
        assertEquals(1, commentRepository.findAllByRecipeIdOrderByCreatedDate(savedRecipe.getId()).size());

        assertFalse(capturedEvents.isEmpty(), "No events were captured!");
        assertEquals(expectedMessage, event.getAction());

        eventCaptureConfig.clearCapturedEvents();

        // Verify that the mocked activity-log-service was called
        wireMockServer.verify(1, WireMock.postRequestedFor(WireMock.urlEqualTo("/api/v1/activity-log")));
    }
}
