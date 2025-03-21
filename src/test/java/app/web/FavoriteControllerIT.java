package app.web;

import app.exception.AlreadyFavoritedException;
import app.exception.FavoriteNotFoundException;
import app.exception.RecipeNotFoundException;
import app.favorite.repository.FavoriteRepository;
import app.favorite.service.FavoriteService;
import app.recipe.model.Recipe;
import app.recipe.repository.RecipeRepository;
import app.recipe.service.RecipeService;
import app.security.WithCustomUser;
import app.user.model.Role;
import app.user.model.User;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithCustomUser(username = "testUser")
@Transactional
public class FavoriteControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private UserService userService;

    @MockitoBean
    private FavoriteService favoriteService;

    private UUID testRecipeId;
    private User testUser;
    private Recipe testRecipe;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("testuser@example.com");
        testUser.setPassword("password123");
        testUser.setRole(Role.USER);
        testUser.setDateRegistered(LocalDateTime.now());
        testUser.setIsActive(true);
        userRepository.save(testUser);

        testRecipe = new Recipe();
        testRecipe.setTitle("Test Recipe");
        testRecipe.setDescription("Test Description");
        testRecipe.setCreatedBy(testUser);
        testRecipe.setCategories(new ArrayList<>());
        testRecipe.setIngredients(Arrays.asList("Salt", "Pepper"));
        testRecipe.setInstructions("Mix everything");
        testRecipe.setCreatedDate(LocalDateTime.now());
        testRecipe.setComments(new ArrayList<>());
        testRecipe.setCookTime(20);
        testRecipe.setServings(2);
        recipeRepository.save(testRecipe);

        testRecipeId = testRecipe.getId();
    }

    @Test
    void testGetFavoriteRecipes_ShouldReturnView() throws Exception {
        mockMvc.perform(get("/favorites"))
                .andExpect(status().isOk())
                .andExpect(view().name("favorite-recipes"))
                .andExpect(model().attributeExists("favoriteRecipes"));
    }

    @Test
    void testAddFavorite_ShouldRedirectToRecipePage() throws Exception {
        mockMvc.perform(post("/favorites/add")
                        .param("recipeId", testRecipeId.toString())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recipes/" + testRecipeId));
    }

    @Test
    void testAddFavorite_WhenRecipeDoesNotExist_ShouldRedirectWithErrorMessage() throws Exception {
        UUID nonExistentRecipeId = UUID.randomUUID();

        doThrow(new RecipeNotFoundException("Recipe with id " + nonExistentRecipeId + " not found."))
                .when(favoriteService).addRecipeToFavorites(any(), any());

        mockMvc.perform(post("/favorites/add")
                        .param("recipeId", nonExistentRecipeId.toString())
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Recipe with id " + nonExistentRecipeId + " not found."));

        // Ensure the favorite was NOT added
        assertFalse(favoriteRepository.existsByRecipeId(nonExistentRecipeId));
    }

    @Test
    void testRemoveFavorite_ShouldRedirectToFavorites() throws Exception {
        mockMvc.perform(delete("/favorites/remove")
                        .param("recipeId", testRecipeId.toString())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/favorites"));
    }

    @Test
    void testAddFavorite_WhenAlreadyFavorited_ShouldShowError() throws Exception {
        doThrow(new AlreadyFavoritedException(testUser.getId(), testRecipeId))
                .when(favoriteService).addRecipeToFavorites(any(), any());

        mockMvc.perform(post("/favorites/add")
                        .param("recipeId", testRecipeId.toString())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("error-page"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Recipe with id " + testRecipeId + " is already in the favorites list of user with id: " + testUser.getId()));
    }

    @Test
    void testRemoveFavorite_WhenFavoriteNotFound_ShouldRedirectWithError() throws Exception {
        doThrow(new FavoriteNotFoundException(testRecipeId))
                .when(favoriteService).removeRecipeFromFavorites(any(), any());

        mockMvc.perform(delete("/favorites/remove")
                        .param("recipeId", testRecipeId.toString())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/favorites"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attribute("errorMessage", "Failed to remove recipe from favorites list"));
    }
}
