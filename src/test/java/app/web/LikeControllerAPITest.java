package app.web;

import app.exception.RecipeAlreadyLikedException;
import app.exception.UserCannotLikeOwnRecipeException;
import app.like.service.LikeService;
import app.recipe.model.Recipe;
import app.security.CustomUserDetails;
import app.user.model.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static app.TestBuilder.aRandomRecipe;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LikeController.class)
public class LikeControllerAPITest {

    @MockitoBean
    private LikeService likeService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testLikeRecipe_ShouldSucceed() throws Exception {
        CustomUserDetails principal = new CustomUserDetails(UUID.randomUUID(), "user", "pass", Role.USER, true);

        Recipe recipe = aRandomRecipe();

        mockMvc.perform(post("/like/{recipeId}", recipe.getId())
                        .with(user(principal))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recipes/" + recipe.getId()));

        verify(likeService, times(1)).like(principal.getId(), recipe.getId());
    }

    @Test
    void testLikeOwnRecipe_ShouldReturnErrorPage() throws Exception {
        CustomUserDetails principal = new CustomUserDetails(UUID.randomUUID(), "user", "pass", Role.USER, true);

        Recipe recipe = aRandomRecipe();
        doThrow(new UserCannotLikeOwnRecipeException("You cannot like your own recipe.")).when(likeService).like(principal.getId(), recipe.getId());

        mockMvc.perform(post("/like/{recipeId}", recipe.getId())
                        .with(user(principal))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("error-page"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void testLikeAlreadyLikedRecipe_ShouldReturnErrorPage() throws Exception {
        CustomUserDetails principal = new CustomUserDetails(UUID.randomUUID(), "user", "pass", Role.USER, true);

        Recipe recipe = aRandomRecipe();

        doThrow(new RecipeAlreadyLikedException("You have already liked this recipe")).when(likeService).like(principal.getId(), recipe.getId());

        mockMvc.perform(post("/like/{recipeId}", recipe.getId())
                        .with(user(principal))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("error-page"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void testLikeWithoutAuthentication_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(post("/like/{recipeId}", UUID.randomUUID())
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}
