package app.web;

import app.comment.model.Comment;
import app.comment.service.CommentService;
import app.recipe.model.Recipe;
import app.security.CustomUserDetails;
import app.user.model.Role;
import app.user.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static app.TestBuilder.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentsApiController.class)
@WithMockUser
public class CommentsApiControllerAPITest {

    @MockitoBean
    private CommentService commentService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetCommentsByRecipe_WhenCommentsExist_ShouldReturn200() throws Exception {
        UUID recipeId = UUID.randomUUID();

        User user = aRandomUser();
        Comment comment = aRandomComment();
        comment.setCreator(user);

        List<Comment> mockComments = List.of(comment);

        when(commentService.getCommentsByRecipeId(recipeId))
                .thenReturn(mockComments);

        mockMvc.perform(get("/api/comments/{recipeId}", recipeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Expect 200 OK
                .andExpect(jsonPath("$.size()").isNotEmpty())
                .andExpect(jsonPath("$[0].content").isNotEmpty())
                .andExpect(jsonPath("$[0].createdBy").isNotEmpty());

        verify(commentService).getCommentsByRecipeId(recipeId);
    }

    @Test
    void testGetCommentsByRecipe_WhenNoComments_ShouldReturn204() throws Exception {
        UUID newRecipeId = UUID.randomUUID();

        mockMvc.perform(get("/api/comments/{recipeId}", newRecipeId))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteComment_WhenUserIsRecipeOwner_ShouldReturn200() throws Exception {
        CustomUserDetails principal = new CustomUserDetails(UUID.randomUUID(), "user", "pass", Role.USER, true);

        User user = aRandomUser();


        user.setUsername(principal.getUsername());
        Comment comment = aRandomComment();
        comment.setCreator(user);

        when(commentService.deleteComment(comment.getId(), user.getUsername())).thenReturn(true);

        mockMvc.perform(delete("/api/comments/delete/{commentId}", comment.getId())
                        .with(user(principal))
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(commentService, times(1)).deleteComment(comment.getId(), user.getUsername());
    }

    @Test
    void testDeleteComment_WhenUserIsCommentOwner_ShouldReturn200() throws Exception {
        CustomUserDetails principal = new CustomUserDetails(UUID.randomUUID(), "user", "pass", Role.USER, true);

        User user = aRandomUser();
        Recipe recipe = aRandomRecipe();
        Comment comment = aRandomComment();
        comment.setCreator(user);
        comment.setRecipe(recipe);

        when(commentService.deleteComment(comment.getId(), principal.getUsername())).thenReturn(true);

        mockMvc.perform(delete("/api/comments/delete/{commentId}", comment.getId())
                        .with(user(principal))
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(commentService, times(1)).deleteComment(comment.getId(), principal.getUsername());
    }


    @Test
    void testDeleteComment_WhenUserDoesNotOwnCommentOrIsNotRecipeCreator_ShouldReturn403() throws Exception {
        CustomUserDetails principal = new CustomUserDetails(UUID.randomUUID(), "user", "pass", Role.USER, true);

        User user = aRandomUser();
        Comment comment = aRandomComment();
        comment.setCreator(user);

        mockMvc.perform(delete("/api/comments/delete/{commentId}", comment.getId())
                        .with(user(principal))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
