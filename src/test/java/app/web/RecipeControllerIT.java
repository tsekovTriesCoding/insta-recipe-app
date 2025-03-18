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
import app.recipe.service.RecipeService;
import app.security.CustomUserDetails;
import app.user.model.Role;
import app.user.model.User;
import app.user.repository.UserRepository;
import app.web.dto.AddRecipe;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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

    @MockitoBean
    private CloudinaryService cloudinaryService;

    @MockitoBean
    private ActivityLogService activityLogService;

    private User user;
    private Recipe recipe;
    private Recipe recipe2;
    private Category category;
    private UUID recipeId;
    private UUID userId;
    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("testUser");
        user.setEmail("testUser@example.com");
        user.setPassword("password");
        user.setRole(Role.USER);
        user.setDateRegistered(LocalDateTime.now());
        user.setIsActive(true);
        user = userRepository.save(user);
        userId = user.getId();

        recipe = new Recipe();
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

        recipe2 = new Recipe();
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
        category = Category.builder()
                .name(CategoryName.DESSERTS)
                .imageUrl("image")
                .build();

        categoryRepository.save(category);

        recipeId = recipe.getId();
        userId = user.getId();
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
        UserDetails userDetails = new CustomUserDetails(user.getId(), user.getUsername(), user.getPassword(), user.getRole(), user.getIsActive());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication); // use this because Spring does not recognize my CustomUserDetails

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
        UserDetails userDetails = new CustomUserDetails(user.getId(), user.getUsername(), user.getPassword(), user.getRole(), user.getIsActive());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

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
    void testAddRecipePage_whenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(get("/recipes/add"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void testAddRecipe_whenValidRecipeIsProvided() throws Exception {
        UserDetails userDetails = new CustomUserDetails(user.getId(), user.getUsername(), user.getPassword(), user.getRole(), user.getIsActive());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        List<CategoryName> categories = List.of(CategoryName.DESSERTS);

        MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", new byte[1]);
        AddRecipe addRecipe = new AddRecipe();
        addRecipe.setTitle("Chocolate Cake");
        addRecipe.setDescription("Delicious homemade chocolate cake.");
        addRecipe.setCategories(categories);
        addRecipe.setImage(image);
        addRecipe.setIngredients("Ingredient1,ingredient2,ingredient3");
        addRecipe.setInstructions("instructions to be made");
        addRecipe.setCookTime(30);
        addRecipe.setServings(4);

        ImageUploadResult mockUploadResult = new ImageUploadResult("http://image.url", "public-id");
        when(cloudinaryService.uploadImage(image)).thenReturn(mockUploadResult);

        MockHttpServletRequestBuilder requestBuilder =
                multipart("/recipes/add")
                        .file(image)  // Add the image file
                        .param("title", addRecipe.getTitle())
                        .param("description", addRecipe.getDescription())
                        .param("categories", String.valueOf(categories.get(0)))
                        .param("ingredients", addRecipe.getIngredients())
                        .param("instructions", addRecipe.getInstructions())
                        .param("cookTime", String.valueOf(addRecipe.getCookTime()))
                        .param("servings", String.valueOf(addRecipe.getServings()))
                        .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/recipes/*")); // Check that the redirect URL is like "/recipe/{id}"
    }
}
