package app.web;

import app.comment.service.CommentService;
import app.exception.RecipeNotFoundException;
import app.recipe.model.Recipe;
import app.security.CustomUserDetails;
import app.user.model.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static app.TestBuilder.aRandomRecipe;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
public class CommentControllerAPITest {

    @MockitoBean
    private CommentService commentService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testAddComment_WhenValidInput_ShouldAddCommentAndRedirect() throws Exception {
        String commentContent = "This is a test comment";

        CustomUserDetails principal = new CustomUserDetails(UUID.randomUUID(), "user", "pass", Role.USER, true);

        Recipe recipe = aRandomRecipe();

        mockMvc.perform(post("/comments/add/{id}", recipe.getId())
                        .param("content", commentContent)
                        .with(user(principal))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recipes/" + recipe.getId()));

        verify(commentService, times(1)).add(commentContent, recipe.getId(), principal.getId());
    }

    @Test
    void testAddComment_WhenEmptyContent_ShouldRedirectWithError() throws Exception {
        CustomUserDetails principal = new CustomUserDetails(UUID.randomUUID(), "user", "pass", Role.USER, true);

        Recipe recipe = aRandomRecipe();

        mockMvc.perform(post("/comments/add/{id}", recipe.getId())
                        .param("content", "")
                        .with(user(principal))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recipes/" + recipe.getId()))
                .andExpect(flash().attributeExists("error"))
                .andExpect(flash().attribute("error", "Please enter a comment"));


        verify(commentService, never()).add("", recipe.getId(), principal.getId());
    }

    @Test
    @WithAnonymousUser
        // Simulate an unauthenticated user
    void testAddComment_WhenNotAuthenticated_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/comments/add/{id}", UUID.randomUUID())
                        .param("content", "Unauthorized comment")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAddComment_WhenRecipeDoesNotExist_ShouldReturnErrorPage() throws Exception {
        CustomUserDetails principal = new CustomUserDetails(UUID.randomUUID(), "user", "pass", Role.USER, true);

        UUID nonExistingRecipeId = UUID.randomUUID();

        doThrow(new RecipeNotFoundException("Recipe with id " + nonExistingRecipeId + " not found."))
                .when(commentService).add(anyString(), eq(nonExistingRecipeId), any());

        mockMvc.perform(post("/comments/add/{id}", nonExistingRecipeId)
                        .param("content", "Test comment")
                        .with(user(principal))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error-page"))
                .andExpect(model().attribute("error", containsString("Recipe with id " + nonExistingRecipeId + " not found.")));
    }

    @Test
    void testAddComment_WhenInvalidRecipeId_ShouldReturnBadRequest() throws Exception {
        CustomUserDetails principal = new CustomUserDetails(UUID.randomUUID(), "user", "pass", Role.USER, true);

        String invalidRecipeId = "invalid-uuid";

        mockMvc.perform(post("/comments/add/{id}", invalidRecipeId)
                        .with(user(principal))
                        .with(csrf())
                        .param("content", "Test comment"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAddComment_WhenContentExceedsMaxLength_ShouldReturnError() throws Exception {
        CustomUserDetails principal = new CustomUserDetails(UUID.randomUUID(), "user", "pass", Role.USER, true);

        String longComment = "a".repeat(501);
        Recipe recipe = aRandomRecipe();

        mockMvc.perform(post("/comments/add/{id}", recipe.getId())
                        .param("content", longComment)
                        .with(user(principal))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recipes/" + recipe.getId()))
                .andExpect(flash().attribute("error", "Content is too long"));

        verify(commentService, never()).add(longComment, recipe.getId(), principal.getId());
    }
}
