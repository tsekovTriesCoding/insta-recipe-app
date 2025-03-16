package app.web;

import app.activitylog.service.ActivityLogService;
import app.comment.model.Comment;
import app.comment.repository.CommentRepository;
import app.recipe.model.Recipe;
import app.recipe.repository.RecipeRepository;
import app.security.WithCustomUser;
import app.user.model.Role;
import app.user.model.User;
import app.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithCustomUser
@Transactional
public class CommentsApiControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private ActivityLogService activityLogService;

    private User recipeOwner;
    private User commentOwner;
    private Recipe testRecipe;
    private UUID testRecipeId;
    private UUID testCommentId;

    @BeforeEach
    void setUp() {
        recipeOwner = new User();
        recipeOwner.setUsername("recipeOwner");
        recipeOwner.setEmail("recipeOwner@example.com");
        recipeOwner.setPassword("password123");
        recipeOwner.setRole(Role.USER);
        recipeOwner.setDateRegistered(LocalDateTime.now());
        recipeOwner.setIsActive(true);
        recipeOwner = userRepository.save(recipeOwner);

        commentOwner = new User();
        commentOwner.setUsername("commentOwner");
        commentOwner.setEmail("commentOwner@example.com");
        commentOwner.setPassword("password");
        commentOwner.setRole(Role.USER);
        commentOwner.setDateRegistered(LocalDateTime.now());
        commentOwner.setIsActive(true);
        commentOwner = userRepository.save(commentOwner);

        testRecipe = new Recipe();
        testRecipe.setTitle("Test Recipe");
        testRecipe.setDescription("Test Description");
        testRecipe.setCreatedBy(recipeOwner);
        testRecipe.setCategories(new ArrayList<>());
        testRecipe.setIngredients(Arrays.asList("Salt", "Pepper"));
        testRecipe.setInstructions("Mix everything");
        testRecipe.setCreatedDate(LocalDateTime.now());
        testRecipe.setComments(new ArrayList<>());
        testRecipe.setCookTime(20);
        testRecipe.setServings(2);
        testRecipe = recipeRepository.save(testRecipe);
        testRecipeId = testRecipe.getId();

        Comment testComment = new Comment();
        testComment.setContent("Test Comment");
        testComment.setRecipe(testRecipe);
        testComment.setCreator(commentOwner);
        testComment = commentRepository.save(testComment);
        testCommentId = testComment.getId();
    }

    @Test
    void testGetCommentsByRecipe_WhenCommentsExist_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/comments/{recipeId}", testRecipeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].content").value("Test Comment"));
    }

    @Test
    void testGetCommentsByRecipe_WhenNoComments_ShouldReturn204() throws Exception {
        UUID newRecipeId = UUID.randomUUID();

        mockMvc.perform(get("/api/comments/{recipeId}", newRecipeId))
                .andExpect(status().isNoContent());
    }

    @WithCustomUser(username = "recipeOwner")
    @Test
    void testDeleteComment_WhenUserIsRecipeOwner_ShouldReturn200() throws Exception {
        mockMvc.perform(delete("/api/comments/delete/{commentId}", testCommentId)
                        .with(csrf()))
                .andExpect(status().isOk());

        // Ensure comment is deleted
        assertFalse(commentRepository.existsById(testCommentId));
    }

    @WithCustomUser(username = "commentOwner")
    @Test
    void testDeleteComment_WhenUserIsCommentOwner_ShouldReturn200() throws Exception {
        mockMvc.perform(delete("/api/comments/delete/{commentId}", testCommentId)
                        .with(csrf()))
                .andExpect(status().isOk());

        // Ensure comment is deleted
        assertFalse(commentRepository.existsById(testCommentId));
    }

    @Test
    void testDeleteComment_WhenUserDoesNotOwnCommentOrIsNotRecipeCreator_ShouldReturn403() throws Exception {
        mockMvc.perform(delete("/api/comments/delete/{commentId}", testCommentId)
                        .with(csrf()))
                .andExpect(status().isForbidden());

        assertTrue(commentRepository.existsById(testCommentId));
    }
}
