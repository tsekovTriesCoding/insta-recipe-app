package app.web;

import app.exception.AlreadyFavoritedException;
import app.exception.FavoriteNotFoundException;
import app.exception.RecipeNotFoundException;
import app.favorite.service.FavoriteService;
import app.security.CustomUserDetails;
import app.user.model.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FavoriteController.class)
public class FavoriteControllerAPITest {

    @MockitoBean
    private FavoriteService favoriteService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetFavoriteRecipes_ShouldReturnView() throws Exception {
        CustomUserDetails principal = new CustomUserDetails(UUID.randomUUID(), "user", "pass", Role.USER, true);

        mockMvc.perform(get("/favorites")
                        .with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("favorite-recipes"))
                .andExpect(model().attributeExists("favoriteRecipes"));
    }

    @Test
    void testAddFavorite_ShouldRedirectToRecipePage() throws Exception {
        CustomUserDetails principal = new CustomUserDetails(UUID.randomUUID(), "user", "pass", Role.USER, true);

        UUID recipeId = UUID.randomUUID();

        mockMvc.perform(post("/favorites/add")
                        .param("recipeId", recipeId.toString())
                        .with(user(principal))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recipes/" + recipeId));
    }

    @Test
    void testAddFavorite_WhenRecipeDoesNotExist_ShouldRedirectWithErrorMessage() throws Exception {
        CustomUserDetails principal = new CustomUserDetails(UUID.randomUUID(), "user", "pass", Role.USER, true);

        UUID nonExistentRecipeId = UUID.randomUUID();

        doThrow(new RecipeNotFoundException("Recipe with id " + nonExistentRecipeId + " not found."))
                .when(favoriteService).addRecipeToFavorites(any(), any());

        mockMvc.perform(post("/favorites/add")
                        .param("recipeId", nonExistentRecipeId.toString())
                        .with(user(principal))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Recipe with id " + nonExistentRecipeId + " not found."));

    }

    @Test
    void testRemoveFavorite_ShouldRedirectToFavorites() throws Exception {
        CustomUserDetails principal = new CustomUserDetails(UUID.randomUUID(), "user", "pass", Role.USER, true);
        UUID recipeId = UUID.randomUUID();

        mockMvc.perform(delete("/favorites/remove")
                        .param("recipeId", recipeId.toString())
                        .with(user(principal))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/favorites"));
    }

    @Test
    void testAddFavorite_WhenAlreadyFavorited_ShouldShowError() throws Exception {
        CustomUserDetails principal = new CustomUserDetails(UUID.randomUUID(), "user", "pass", Role.USER, true);

        UUID recipeId = UUID.randomUUID();

        doThrow(new AlreadyFavoritedException(principal.getId(), recipeId))
                .when(favoriteService).addRecipeToFavorites(principal.getId(), recipeId);

        mockMvc.perform(post("/favorites/add")
                        .param("recipeId", recipeId.toString())
                        .with(user(principal))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("error-page"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Recipe with id " + recipeId + " is already in the favorites list of user with id: " + principal.getId()));
    }

    @Test
    void testRemoveFavorite_WhenFavoriteNotFound_ShouldRedirectWithError() throws Exception {
        CustomUserDetails principal = new CustomUserDetails(UUID.randomUUID(), "user", "pass", Role.USER, true);

        UUID recipeId = UUID.randomUUID();

        doThrow(new FavoriteNotFoundException(recipeId))
                .when(favoriteService).removeRecipeFromFavorites(principal.getId(), recipeId);

        mockMvc.perform(delete("/favorites/remove")
                        .param("recipeId", recipeId.toString())
                        .with(user(principal))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/favorites"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attribute("errorMessage", "Failed to remove recipe from favorites list"));
    }
}
