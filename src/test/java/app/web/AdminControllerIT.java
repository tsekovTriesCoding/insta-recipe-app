package app.web;

import app.comment.model.Comment;
import app.comment.service.CommentService;
import app.exception.RecipeNotFoundException;
import app.recipe.model.Recipe;
import app.recipe.service.RecipeService;
import app.user.model.User;
import app.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static app.TestBuilder.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@WithMockUser
class AdminControllerIT {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private RecipeService recipeService;

    @MockitoBean
    private CommentService commentService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetAdminUserManagement() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-user-management"))
                .andExpect(model().attributeExists("users"));

        verify(userService, times(1)).getAll();
    }

    @Test
    void testChangeUserRole() throws Exception {
        User user = aRandomUser();

        doNothing().when(userService).changeUserRole(user.getId());

        mockMvc.perform(put("/admin/users/change-role/{userId}", user.getId())
                        .with(csrf())) //this or create test configuration and disable CSRF for tests!
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users?message=" + URLEncoder.encode("You have successfully changed the user's role", StandardCharsets.UTF_8)));// this, because the browser handles white spaces and other chars differently!

        verify(userService, times(1)).changeUserRole(user.getId());
    }

    @Test
    void testChangeUserStatus() throws Exception {
        User user = aRandomUser();

        doNothing().when(userService).changeUserStatus(user.getId());

        mockMvc.perform(put("/admin/users/change-status/{userId}", user.getId())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users?message=" + URLEncoder.encode("You have successfully changed the user's status", StandardCharsets.UTF_8)));

        verify(userService, times(1)).changeUserStatus(user.getId());
    }

    @Test
    void testGetAdminRecipeManagement() throws Exception {
        mockMvc.perform(get("/admin/recipes"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-recipe-management"))
                .andExpect(model().attributeExists("recipes"));

        verify(recipeService, times(1)).getAllForAdmin();
    }

    @Test
    void testDeleteRecipe_WhenRecipeExists_ShouldDeleteSuccessfully() throws Exception {
        Recipe recipe = aRandomRecipe();

        mockMvc.perform(delete("/admin/recipes/{recipeId}", recipe.getId())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/recipes?message=" + URLEncoder.encode("You have successfully deleted recipe with [id]: " + recipe.getId(), StandardCharsets.UTF_8)));

        verify(recipeService, times(1)).deleteByAdmin(recipe.getId());
    }

    @Test
    public void testDeleteRecipe_WhenRecipeDoesNotExist_ShouldReturnErrorPage() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        doThrow(new RecipeNotFoundException("Recipe with id " + nonExistentId + " not found."))
                .when(recipeService).deleteByAdmin(nonExistentId);

        mockMvc.perform(delete("/admin/recipes/" + nonExistentId)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error-page")) // Ensure it loads the correct error page
                .andExpect(model().attributeExists("error")) // Check if the message is present
                .andExpect(model().attribute("error", "Recipe with id " + nonExistentId + " not found."));
    }


    @Test
    void testGetAdminCommentManagement() throws Exception {
        mockMvc.perform(get("/admin/comments"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-comment-management"))
                .andExpect(model().attributeExists("comments"));

        verify(commentService, times(1)).getAll();
    }

    @Test
    void testDeleteComment() throws Exception {
        Comment comment = aRandomComment();

        mockMvc.perform(delete("/admin/comments/{commentId}", comment.getId())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/comments?message=" + URLEncoder.encode("You have successfully deleted comment with [id]: " + comment.getId(), StandardCharsets.UTF_8)));

        verify(commentService, times(1)).delete(comment.getId());
    }
}