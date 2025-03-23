package app.recipe;

import app.activitylog.event.ActivityLogEvent;
import app.category.model.Category;
import app.category.model.CategoryName;
import app.category.repository.CategoryRepository;
import app.category.service.CategoryService;
import app.cloudinary.dto.ImageUploadResult;
import app.cloudinary.service.CloudinaryService;
import app.recipe.model.Recipe;
import app.recipe.repository.RecipeRepository;
import app.recipe.service.RecipeService;
import app.user.model.User;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.web.dto.AddRecipe;
import app.web.dto.EditRecipe;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static app.TestBuilder.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RecipeServiceIT {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @MockitoBean
    private CloudinaryService cloudinaryService;

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

        Category randomCategory1 = aRandomCategoryWithoutId();
        Category randomCategory2 = aRandomCategoryWithoutId();
        randomCategory2.setName(CategoryName.VEGAN);

        categoryRepository.saveAll(List.of(randomCategory1, randomCategory2));
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
    public void testCreate_shouldCreateRecipe() {
        AddRecipe addRecipe = aRandomAddRecipe();
        User randomUser = aRandomWithoutId();

        User user = userRepository.save(randomUser);

        MultipartFile newImage = mock(MultipartFile.class);
        addRecipe.setImage(newImage);
        ImageUploadResult uploadResult = new ImageUploadResult("http://newimageurl.com", "newPublicId");

        // Mock Cloudinary service's uploadImage method
        when(cloudinaryService.uploadImage(newImage)).thenReturn(uploadResult);

        Recipe recipe = recipeService.create(addRecipe, user.getId());

        List<ActivityLogEvent> capturedEvents = eventCaptureConfig.getCapturedEvents();
        ActivityLogEvent event = capturedEvents.get(0);
        String expectedMessage = "You have successfully added recipe: " + recipe.getTitle();

        assertEquals("newPublicId", recipe.getImagePublicId());
        assertEquals("http://newimageurl.com", recipe.getImage());

        assertFalse(capturedEvents.isEmpty(), "No events were captured!");
        assertEquals(expectedMessage, event.getAction());

        eventCaptureConfig.clearCapturedEvents();
    }

    @Test
    void testUpdate_shouldUpdateRecipe() {
        User user = aRandomWithoutId();
        EditRecipe editRecipe = EditRecipe.builder()
                .title("newTitle")
                .description("newDescription")
                .instructions("newInstructions")
                .categories(List.of(CategoryName.MAIN_COURSE))
                .ingredients("ingredient1, ingredient2")
                .cookTime(5)
                .prepTime(6)
                .servings(7)
                .build();

        Recipe recipe = aRandomRecipeWithoutId();
        recipe.setCreatedBy(user);
        recipe.setImagePublicId("oldPublicId");

        userRepository.save(user);

        Recipe save = recipeRepository.save(recipe);
        editRecipe.setId(save.getId());

        // Mock the CloudinaryService to simulate deleting the old image
        doNothing().when(cloudinaryService).deleteImage("oldPublicId");

        // Mock the uploadImage method to simulate uploading the new image
        MultipartFile newImage = mock(MultipartFile.class);
        editRecipe.setImage(newImage);
        ImageUploadResult uploadResult = new ImageUploadResult("newPublicId", "http://newimageurl.com");
        when(cloudinaryService.uploadImage(newImage)).thenReturn(uploadResult);

        recipeService.update(editRecipe);

        Recipe updatedRecipe = recipeRepository.findById(save.getId()).get();

        List<ActivityLogEvent> capturedEvents = eventCaptureConfig.getCapturedEvents();
        ActivityLogEvent event = capturedEvents.get(0);
        String expectedMessage = "You have successfully updated recipe: " + updatedRecipe.getTitle();

        assertNotEquals(updatedRecipe.getTitle(), recipe.getTitle());
        assertNotEquals(updatedRecipe.getDescription(), recipe.getDescription());
        assertNotEquals(updatedRecipe.getCategories(), recipe.getCategories());
        assertNotEquals(updatedRecipe.getInstructions(), recipe.getInstructions());
        assertNotEquals(updatedRecipe.getIngredients(), recipe.getIngredients());
        assertNotEquals(updatedRecipe.getCookTime(), recipe.getCookTime());
        assertNotEquals(updatedRecipe.getPrepTime(), recipe.getPrepTime());
        assertNotEquals(updatedRecipe.getServings(), recipe.getServings());
        assertNotEquals(updatedRecipe.getImage(), recipe.getImage());
        assertNotEquals(updatedRecipe.getImagePublicId(), recipe.getImagePublicId());

        assertFalse(capturedEvents.isEmpty(), "No events were captured!");
        assertEquals(expectedMessage, event.getAction());

        eventCaptureConfig.clearCapturedEvents();
    }

    @Test
    void testDelete_shouldDeleteRecipe() {
        User user = aRandomWithoutId();
        Recipe recipe = aRandomRecipeWithoutId();
        recipe.setImagePublicId("oldPublicId");
        recipe.setCreatedBy(user);

        userRepository.save(user);
        Recipe savedRecipe = recipeRepository.save(recipe);

        doNothing().when(cloudinaryService).deleteImage("oldPublicId");

        recipeService.delete(savedRecipe.getId());

        List<ActivityLogEvent> capturedEvents = eventCaptureConfig.getCapturedEvents();
        ActivityLogEvent event = capturedEvents.get(0);
        String expectedMessage = "You have successfully deleted recipe: " + recipe.getTitle();

        assertFalse(recipeRepository.findById(savedRecipe.getId()).isPresent());

        assertFalse(capturedEvents.isEmpty(), "No events were captured!");
        assertEquals(expectedMessage, event.getAction());

        eventCaptureConfig.clearCapturedEvents();

        verify(cloudinaryService, times(1)).deleteImage("oldPublicId");
    }

    @Test
    void testDeleteByAdmin_shouldDeleteRecipe() {
        User user = aRandomWithoutId();
        Recipe recipe = aRandomRecipeWithoutId();
        recipe.setImagePublicId("oldPublicId");
        recipe.setCreatedBy(user);

        userRepository.save(user);
        Recipe savedRecipe = recipeRepository.save(recipe);

        doNothing().when(cloudinaryService).deleteImage("oldPublicId");

        recipeService.deleteByAdmin(savedRecipe.getId());

        assertFalse(recipeRepository.findById(savedRecipe.getId()).isPresent());

        verify(cloudinaryService, times(1)).deleteImage("oldPublicId");
    }
}
