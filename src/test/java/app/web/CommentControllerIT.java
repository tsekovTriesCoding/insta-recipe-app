package app.web;

import app.activitylog.service.ActivityLogService;
import app.comment.repository.CommentRepository;
import app.recipe.model.Recipe;
import app.recipe.repository.RecipeRepository;
import app.security.CustomUserDetails;
import app.user.model.Role;
import app.user.model.User;
import app.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "testuser", roles = "USER")
public class CommentControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private ActivityLogService activityLogService; // I don't want a real call to the microservice

    private Recipe testRecipe;
    private User testUser;

    @BeforeEach
    void setUp() {
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
    }

    @Test
    void testAddComment_WhenValidInput_ShouldAddCommentAndRedirect() throws Exception {
        String commentContent = "This is a test comment";

        UserDetails userDetails = new CustomUserDetails(testUser.getId(), "testuser", "password", Role.USER, true);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);// manually set the authentication context using SecurityContextHolder because when it gets to the add method the AuthenticationPrincipal is null!!

        mockMvc.perform(post("/comments/add/{id}", testRecipe.getId())
                        .param("content", commentContent)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recipes/" + testRecipe.getId()));

        // Verify the comment is saved
        assertTrue(commentRepository.existsByContent(commentContent));
    }

    @Test
    void testAddComment_WhenEmptyContent_ShouldRedirectWithError() throws Exception {
        mockMvc.perform(post("/comments/add/{id}", testRecipe.getId())
                        .param("content", "")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recipes/" + testRecipe.getId()))
                .andExpect(flash().attributeExists("error"))  // Check error message exists
                .andExpect(flash().attribute("error", "Please enter a comment"));

        // Ensure no comment was saved
        assertEquals(0, commentRepository.count());
    }

    @Test
    @WithAnonymousUser
        // Simulate an unauthenticated user
    void testAddComment_WhenNotAuthenticated_ShouldRedirectToLoginPage() throws Exception {
        mockMvc.perform(post("/comments/add/{id}", testRecipe.getId())
                        .param("content", "Unauthorized comment")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/users/login"));

        // Ensure no comment was saved
        assertEquals(0, commentRepository.count());
    }

    @Test
    void testAddComment_WhenRecipeDoesNotExist_ShouldReturnErrorPage() throws Exception {
        UUID nonExistingRecipeId = UUID.randomUUID();

        UserDetails userDetails = new CustomUserDetails(testUser.getId(), "testuser", "password", Role.USER, true);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        mockMvc.perform(post("/comments/add/{id}", nonExistingRecipeId)
                        .param("content", "Test comment")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error-page"))
                .andExpect(model().attribute("error", containsString("Recipe with id " + nonExistingRecipeId + " not found.")));
    }

    @Test
    void testAddComment_WhenInvalidRecipeId_ShouldReturnBadRequest() throws Exception {
        String invalidRecipeId = "invalid-uuid";

        mockMvc.perform(post("/comments/add/{id}", invalidRecipeId)
                        .param("content", "Test comment")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAddComment_WhenContentExceedsMaxLength_ShouldReturnError() throws Exception {
        String longComment = "a".repeat(501);

        UserDetails userDetails = new CustomUserDetails(testUser.getId(), "testuser", "password", Role.USER, true);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        mockMvc.perform(post("/comments/add/{id}", testRecipe.getId())
                        .param("content", longComment)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recipes/" + testRecipe.getId()))
                .andExpect(flash().attribute("error", "Content is too long"));
    }
}
