package app.web;

import app.activitylog.service.ActivityLogService;
import app.category.model.Category;
import app.category.model.CategoryName;
import app.category.repository.CategoryRepository;
import app.cloudinary.dto.ImageUploadResult;
import app.cloudinary.service.CloudinaryService;
import app.favorite.service.FavoriteService;
import app.like.service.LikeService;
import app.recipe.model.Recipe;
import app.recipe.repository.RecipeRepository;
import app.security.CustomUserDetails;
import app.user.model.Role;
import app.user.model.User;
import app.user.repository.UserRepository;
import app.web.dto.AddRecipe;
import app.web.dto.EditRecipe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class RecipeControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private CategoryRepository categoryRepository;

    @MockitoBean
    private CloudinaryService cloudinaryService;

    @MockitoBean
    private ActivityLogService activityLogService;

    private UUID recipeId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setUsername("testUser");
        user.setEmail("testUser@example.com");
        user.setPassword("password");
        user.setRole(Role.USER);
        user.setDateRegistered(LocalDateTime.now());
        user.setIsActive(true);
        user = userRepository.save(user);
        userId = user.getId();

        Recipe recipe = new Recipe();
        recipe.setTitle("Test Recipe");
        recipe.setDescription("Test Description");
        recipe.setCreatedBy(user);
        recipe.setCategories(new ArrayList<>());
        recipe.setIngredients(Arrays.asList("Salt", "Pepper"));
        recipe.setInstructions("Mix everything");
        recipe.setCreatedDate(LocalDateTime.now());
        recipe.setComments(new ArrayList<>());
        recipe.setCookTime(20);
        recipe.setServings(2);
        recipeRepository.save(recipe);

        Recipe recipe2 = new Recipe();
        recipe2.setTitle("Cake");
        recipe2.setDescription("Cake description");
        recipe2.setCreatedBy(user);
        recipe2.setCategories(new ArrayList<>());
        recipe2.setIngredients(Arrays.asList("Salt", "Pepper"));
        recipe2.setInstructions("Mix everything");
        recipe2.setCreatedDate(LocalDateTime.now());
        recipe2.setComments(new ArrayList<>());
        recipe2.setCookTime(20);
        recipe2.setServings(2);
        recipeRepository.save(recipe2);

        Category category = Category.builder()
                .name(CategoryName.DESSERTS)
                .imageUrl("image")
                .build();

        categoryRepository.save(category);

        recipeId = recipe.getId();
        userId = user.getId();

        UserDetails userDetails = new CustomUserDetails(user.getId(), user.getUsername(), user.getPassword(), user.getRole(), user.getIsActive());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication); // use this because Spring does not recognize my CustomUserDetails
    }

    @Test
    @WithMockUser
    void testGetAllRecipes_whenDatabaseIsEmpty() throws Exception {
        recipeRepository.deleteAll();

        mockMvc.perform(get("/recipes/all")
                        .param("page", "0")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(view().name("recipes"))
                .andExpect(model().attributeExists("recipes"))
                .andExpect(model().attribute("recipes", hasProperty("content", hasSize(0))))
                .andExpect(model().attribute("recipes", hasProperty("numberOfElements", is(0))));
    }

    @Test
    @WithMockUser
    void testGetAllRecipes_whenDatabaseHasRecipes() throws Exception {
        mockMvc.perform(get("/recipes/all")
                        .param("page", "0")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(view().name("recipes"))
                .andExpect(model().attributeExists("recipes"))
                .andExpect(model().attribute("recipes", hasProperty("content", hasSize(Math.toIntExact(2L)))))
                .andExpect(model().attribute("recipes", hasProperty("totalElements", is(2L))));
    }

    @Test
    @WithMockUser
    void testSearchRecipes_withMatchingResults() throws Exception {
        mockMvc.perform(get("/recipes/all").param("query", "Test Recipe"))
                .andExpect(status().isOk())
                .andExpect(view().name("recipes"))
                .andExpect(model().attribute("recipes", hasProperty("content", hasSize(Math.toIntExact(1L)))))
                .andExpect(model().attribute("recipes", hasProperty("totalElements", is(1L))));
    }

    @Test
    @WithMockUser
    void testSearchRecipes_withNoMatchingResults_returnsAll() throws Exception {
        mockMvc.perform(get("/recipes/all").param("query", "Pizza"))
                .andExpect(status().isOk())
                .andExpect(view().name("recipes"))
                .andExpect(model().attribute("recipes", hasProperty("content", hasSize(Math.toIntExact(2L)))))
                .andExpect(model().attribute("recipes", hasProperty("totalElements", is(2L))));
    }

    @Test
    @WithMockUser
    void testGetAllRecipes_whenPageDoesNotExist() throws Exception {
        mockMvc.perform(get("/recipes/all")
                        .param("page", "1000")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("recipes"))
                .andExpect(model().attribute("recipes", hasProperty("content", hasSize(0))));
    }

    @Test
    void testRecipeDetails_whenRecipeExists() throws Exception {
        mockMvc.perform(get("/recipes/" + recipeId))
                .andExpect(status().isOk())
                .andExpect(view().name("recipe-details"))
                .andExpect(model().attributeExists("recipe"))
                .andExpect(model().attribute("recipe", hasProperty("title", is("Test Recipe"))))
                .andExpect(model().attribute("recipe", hasProperty("description", is("Test Description"))))
                .andExpect(model().attribute("recipe", hasProperty("ingredients", hasSize(2))))
                .andExpect(model().attribute("recipe", hasProperty("instructions", is("Mix everything"))))
                .andExpect(model().attribute("isCreator", is(true)))
                .andExpect(model().attribute("hasLiked", is(false)))
                .andExpect(model().attribute("isFavorite", is(false)));
    }

    @Test
    void testRecipeDetails_whenUserIsNotCreator() throws Exception {
        UserDetails userDetails = new CustomUserDetails(UUID.randomUUID(), "Random User", "pass", Role.USER, true);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        mockMvc.perform(get("/recipes/" + recipeId))
                .andExpect(status().isOk())
                .andExpect(view().name("recipe-details"))
                .andExpect(model().attributeExists("recipe"))
                .andExpect(model().attribute("recipe", hasProperty("title", is("Test Recipe"))))
                .andExpect(model().attribute("recipe", hasProperty("description", is("Test Description"))))
                .andExpect(model().attribute("recipe", hasProperty("ingredients", hasSize(2))))
                .andExpect(model().attribute("recipe", hasProperty("instructions", is("Mix everything"))))
                .andExpect(model().attribute("isCreator", is(false)))
                .andExpect(model().attribute("hasLiked", is(false)))
                .andExpect(model().attribute("isFavorite", is(false)));
    }

    @Test
    @WithMockUser
    void testRecipeDetails_whenRecipeDoesNotExist() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/recipes/" + nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testRecipeDetails_whenIdIsInvalid() throws Exception {
        mockMvc.perform(get("/recipes/invalid-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRecipeDetails_whenUserHasLikedRecipe() throws Exception {
        User user2 = new User();
        user2.setUsername("newUser");
        user2.setEmail("newUser@example.com");
        user2.setPassword("password");
        user2.setRole(Role.USER);
        user2.setDateRegistered(LocalDateTime.now());
        user2.setIsActive(true);
        user2 = userRepository.save(user2);
        UUID user2Id = user2.getId();

        UserDetails userDetails = new CustomUserDetails(user2Id, user2.getUsername(), user2.getPassword(), user2.getRole(), user2.getIsActive());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        likeService.like(user2Id, recipeId); // Mock user like

        mockMvc.perform(get("/recipes/" + recipeId))
                .andExpect(status().isOk())
                .andExpect(model().attribute("hasLiked", is(true)));
    }

    @Test
    void testRecipeDetails_whenUserHasFavoritedRecipe() throws Exception {
        favoriteService.addRecipeToFavorites(userId, recipeId); // Mock favorite action

        mockMvc.perform(get("/recipes/" + recipeId))
                .andExpect(status().isOk())
                .andExpect(model().attribute("isCreator", is(true)))
                .andExpect(model().attribute("hasLiked", is(false)))
                .andExpect(model().attribute("isFavorite", is(true)));
    }

    @Test
    @WithMockUser
    void testAddRecipePage_whenUserIsAuthenticated() throws Exception {
        mockMvc.perform(get("/recipes/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-recipe"))
                .andExpect(model().attributeExists("addRecipe"))
                .andExpect(model().attribute("addRecipe", instanceOf(AddRecipe.class)));
    }

    @Test
    @WithMockUser
    void testAddRecipePage_whenUserIsNotAuthenticated() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(null);

        mockMvc.perform(get("/recipes/add"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void testAddRecipe_whenValidRecipeIsProvided() throws Exception {
        List<CategoryName> categories = List.of(CategoryName.DESSERTS);

        MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", new byte[1]);
        AddRecipe addRecipe = new AddRecipe();
        addRecipe.setTitle("Chocolate Cake");
        addRecipe.setDescription("Delicious homemade chocolate cake.");
        addRecipe.setImage(image);
        addRecipe.setCategories(categories);
        addRecipe.setImage(image);
        addRecipe.setIngredients("Ingredient1,ingredient2,ingredient3");
        addRecipe.setInstructions("instructions to be made");
        addRecipe.setCookTime(30);
        addRecipe.setServings(4);

        ImageUploadResult mockUploadResult = new ImageUploadResult("http://image.url", "public-id");
        when(cloudinaryService.uploadImage(image)).thenReturn(mockUploadResult);

        mockMvc.perform(post("/recipes/add")
                        .with(csrf())
                        .flashAttr("addRecipe", addRecipe))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/recipes/*")); // Check that the redirect URL is like "/recipe/{id}"
    }

    @Test
    void testAddRecipe_whenInvalidRecipeIsProvided() throws Exception {
        mockMvc.perform(post("/recipes/add")
                        .with(csrf())// Enable CSRF token for security
                        // with no image
                        .param("title", "") // Empty title (invalid)
                        .param("description", "Short") // Too short description (invalid)
                        .param("categories", String.valueOf(new ArrayList<>())) // No categories
                        .param("ingredients", "") // Empty ingredients
                        .param("instructions", "") // Empty instructions
                        .param("cookTime", "-10") // Negative time (invalid)
                        .param("servings", "0")) // Invalid servings
                .andExpect(status().isOk()) // Should return 200 (stay on the same page)
                .andExpect(view().name("add-recipe")) // Should not redirect, but stay on form page
                .andExpect(model().attributeHasFieldErrors("addRecipe", "title", "description", "image", "categories", "cookTime", "servings"));
    }

    @Test
    void testEditRecipe_WhenRecipeExists() throws Exception {
        mockMvc.perform(get("/recipes/edit/" + recipeId))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-recipe"))
                .andExpect(model().attributeExists("editRecipe"))
                .andExpect(model().attribute("editRecipe", hasProperty("title", is("Test Recipe"))))
                .andExpect(model().attribute("editRecipe", hasProperty("description", is("Test Description"))))
                .andExpect(model().attribute("editRecipe", hasProperty("categories", is(emptyCollectionOf(CategoryName.class)))))
                .andExpect(model().attribute("editRecipe", hasProperty("ingredients", is("Salt,Pepper"))))
                .andExpect(model().attribute("editRecipe", hasProperty("instructions", is("Mix everything"))))
                .andExpect(model().attribute("editRecipe", hasProperty("cookTime", is(20))))
                .andExpect(model().attribute("editRecipe", hasProperty("servings", is(2))));
    }

    @Test
    @WithMockUser
    void testEditRecipe_WhenRecipeDoesNotExist() throws Exception {
        UUID invalidId = UUID.randomUUID();

        mockMvc.perform(get("/recipes/edit/" + invalidId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testEditRecipe_WhenValidRecipeProvided() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", new byte[1]);

        Category newCategory = Category.builder()
                .name(CategoryName.MAIN_COURSE)
                .description("Test Description")
                .imageUrl("image")
                .build();

        categoryRepository.save(newCategory);

        ImageUploadResult mockUploadResult = new ImageUploadResult("http://image.url", "public-id");
        when(cloudinaryService.uploadImage(image)).thenReturn(mockUploadResult);

        EditRecipe editRecipe = EditRecipe.builder()
                .title("Updated Recipe Title")
                .description("Updated Recipe Description")
                .image(image)
                .categories(List.of(CategoryName.MAIN_COURSE))
                .ingredients("new,ingredients,to,add")
                .instructions("Updated instruction to test")
                .cookTime(5)
                .servings(4)
                .build();

        mockMvc.perform(post("/recipes/edit/" + recipeId)
                        .with(csrf())
                        .flashAttr("editRecipe", editRecipe)) // Simulating form submission
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recipes/" + recipeId));

    }

    @Test
    @WithMockUser
    void testEditRecipe_WhenInvalidRecipeProvided() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", new byte[(3 * 1024 * 1024) + 1]);

        EditRecipe editRecipe = EditRecipe.builder()
                .title("")
                .description("Short")
                .image(image)
                .categories(new ArrayList<>())
                .ingredients("")
                .instructions("")
                .cookTime(-5)
                .servings(0)
                .build();

        mockMvc.perform(post("/recipes/edit/" + recipeId)
                        .with(csrf())
                        .flashAttr("editRecipe", editRecipe))
                .andExpect(status().isOk()) // Should return 200 (stay on the same page)
                .andExpect(view().name("edit-recipe")) // Should not redirect, but stay on form page
                .andExpect(model().attributeHasFieldErrors("editRecipe", "title", "description", "image", "categories", "cookTime", "servings"));
    }

    @Test
    void testMyRecipes_WhenUserHasRecipes() throws Exception {
        mockMvc.perform(get("/recipes/my-recipes"))
                .andExpect(status().isOk())
                .andExpect(view().name("my-recipes"))
                .andExpect(model().attributeExists("myRecipes"))
                .andExpect(model().attribute("myRecipes", hasSize(2)))
                .andExpect(model().attribute("myRecipes", hasItem(hasProperty("title", is("Test Recipe")))))
                .andExpect(model().attribute("myRecipes", hasItem(hasProperty("title", is("Cake")))));

    }

    @Test
    void testMyRecipes_WhenUserHasNoRecipes() throws Exception {
        recipeRepository.deleteAll();

        mockMvc.perform(get("/recipes/my-recipes"))
                .andExpect(status().isOk())
                .andExpect(view().name("my-recipes"))
                .andExpect(model().attributeExists("myRecipes"))
                .andExpect(model().attribute("myRecipes", hasSize(0)));
    }

    @Test
    void testMyRecipes_WhenUserIsNotAuthenticated() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(null);

        mockMvc.perform(get("/recipes/my-recipes"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/users/login"));
    }

    @Test
    @WithMockUser
    void testDeleteRecipe_WhenRecipeExists() throws Exception {
        mockMvc.perform(delete("/recipes/delete/" + recipeId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recipes/my-recipes"));
    }

    @Test
    @WithMockUser
    void testDeleteRecipe_WhenRecipeDoesNotExist() throws Exception {
        UUID recipeId = UUID.randomUUID();

        mockMvc.perform(delete("/recipes/delete/" + recipeId)
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
}
