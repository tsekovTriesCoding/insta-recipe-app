package app.web;

import app.activitylog.client.ActivityLogClient;
import app.comment.model.Comment;
import app.comment.repository.CommentRepository;
import app.recipe.model.Recipe;
import app.recipe.repository.RecipeRepository;
import app.user.model.Role;
import app.user.model.User;
import app.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
@ActiveProfiles("test")
class AdminControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private ActivityLogClient activityLogClient;

    private User adminUser;
    private User normalUser;
    private Recipe testRecipe;
    private Comment testComment;

    @BeforeEach
    void setUp() {
        // Create an Admin User
        adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode("adminpass"));
        adminUser.setRole(Role.ADMIN);
        adminUser.setDateRegistered(LocalDateTime.now());
        adminUser.setIsActive(true);
        userRepository.save(adminUser);

        // Create a Normal User
        normalUser = new User();
        normalUser.setUsername("user");
        normalUser.setEmail("user@example.com");
        normalUser.setPassword(passwordEncoder.encode("userpass"));
        normalUser.setRole(Role.USER);
        normalUser.setDateRegistered(LocalDateTime.now());
        normalUser.setIsActive(true);
        userRepository.save(normalUser);

        // Create a Test Recipe
        testRecipe = new Recipe();
        testRecipe.setTitle("Test Recipe");
        testRecipe.setCreatedBy(normalUser);
        testRecipe.setDescription("Test Description");
        testRecipe.setCategories(new ArrayList<>());
        testRecipe.setIngredients(Arrays.asList("Salt", "Pepper"));
        testRecipe.setInstructions("Mix everything");
        testRecipe.setCreatedDate(LocalDateTime.now());
        testRecipe.setComments(new ArrayList<>());
        testRecipe.setCookTime(20);
        testRecipe.setServings(2);
        recipeRepository.save(testRecipe);

        // Create a Test Comment
        testComment = new Comment();
        testComment.setContent("Test Comment");
        testComment.setCreator(normalUser);
        testComment.setCreatedDate(LocalDateTime.now());
        testComment.setRecipe(testRecipe);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAdminUserManagement() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-user-management"))
                .andExpect(model().attributeExists("users"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testChangeUserRole() throws Exception {
        mockMvc.perform(put("/admin/users/change-role/{userId}", normalUser.getId())
                        .with(csrf())) //this or create test configuration and disable CSRF for tests!
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users?message=" + URLEncoder.encode("You have successfully changed the user's role", StandardCharsets.UTF_8)));// this, because the browser handles white spaces and other chars differently!

        User updatedUser = userRepository.findById(normalUser.getId()).orElseThrow();
        assertEquals(Role.ADMIN, updatedUser.getRole());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testChangeUserStatus() throws Exception {
        mockMvc.perform(put("/admin/users/change-status/{userId}", normalUser.getId())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users?message=" + URLEncoder.encode("You have successfully changed the user's status", StandardCharsets.UTF_8)));

        User updatedUser = userRepository.findById(normalUser.getId()).orElseThrow();
        assertFalse(updatedUser.getIsActive());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAdminRecipeManagement() throws Exception {
        mockMvc.perform(get("/admin/recipes"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-recipe-management"))
                .andExpect(model().attributeExists("recipes"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteRecipe() throws Exception {
        mockMvc.perform(delete("/admin/recipes/{recipeId}", testRecipe.getId())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/recipes?message=" + URLEncoder.encode("You have successfully deleted recipe with [id]: " + testRecipe.getId(), StandardCharsets.UTF_8)));

        given(activityLogClient.logActivity(any())).willReturn(ResponseEntity.ok().build());

        assertFalse(recipeRepository.existsById(testRecipe.getId()));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAdminCommentManagement() throws Exception {
        mockMvc.perform(get("/admin/comments"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-comment-management"))
                .andExpect(model().attributeExists("comments"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteComment() throws Exception {
        commentRepository.save(testComment);

        mockMvc.perform(delete("/admin/comments/{commentId}", testComment.getId())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/comments?message=" + URLEncoder.encode("You have successfully deleted comment with [id]: " + testComment.getId(), StandardCharsets.UTF_8)));

        assertFalse(commentRepository.existsById(testComment.getId()));
    }
}