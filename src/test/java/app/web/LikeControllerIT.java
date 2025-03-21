package app.web;

import app.activitylog.service.ActivityLogService;
import app.like.repository.LikeRepository;
import app.recipe.model.Recipe;
import app.recipe.repository.RecipeRepository;
import app.security.CustomUserDetails;
import app.user.model.Role;
import app.user.model.User;
import app.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class LikeControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private LikeRepository likeRepository;

    @MockitoBean
    private ActivityLogService activityLogService;

    private User user;
    private User recipeOwner;
    private UUID recipeId;
    private UUID userId;

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

        recipeOwner = new User();
        recipeOwner.setUsername("recipeOwner");
        recipeOwner.setEmail("recipeOwner@example.com");
        recipeOwner.setPassword("password");
        recipeOwner.setDateRegistered(LocalDateTime.now());
        recipeOwner.setRole(Role.USER);
        recipeOwner.setIsActive(true);
        recipeOwner = userRepository.save(recipeOwner);

        Recipe recipe = new Recipe();
        recipe.setTitle("Test Recipe");
        recipe.setDescription("Test Description");
        recipe.setCreatedBy(recipeOwner);
        recipe.setCategories(new ArrayList<>());
        recipe.setIngredients(Arrays.asList("Salt", "Pepper"));
        recipe.setInstructions("Mix everything");
        recipe.setCreatedDate(LocalDateTime.now());
        recipe.setComments(new ArrayList<>());
        recipe.setCookTime(20);
        recipe.setServings(2);
        recipeRepository.save(recipe);

        recipeId = recipe.getId();
    }

    @AfterEach
    void tearDown() {
        recipeRepository.deleteAll();
        userRepository.deleteAll();
        likeRepository.deleteAll();
    }

    @Test
    void testLikeRecipe_ShouldSucceed() throws Exception {
        UserDetails userDetails = new CustomUserDetails(userId, user.getUsername(), user.getPassword(), user.getRole(), user.getIsActive());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        mockMvc.perform(post("/like/{recipeId}", recipeId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recipes/" + recipeId));

        // Verify the recipe is liked
        assertTrue(likeRepository.existsByUser_IdAndRecipe_Id(userId, recipeId));
    }

    @Test
    void testLikeOwnRecipe_ShouldReturnErrorPage() throws Exception {
        UserDetails userDetails = new CustomUserDetails(recipeOwner.getId(), recipeOwner.getUsername(), recipeOwner.getPassword(), recipeOwner.getRole(), recipeOwner.getIsActive());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        mockMvc.perform(post("/like/{recipeId}", recipeId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("error-page"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void testLikeAlreadyLikedRecipe_ShouldReturnErrorPage() throws Exception {
        UserDetails userDetails = new CustomUserDetails(userId, user.getUsername(), user.getPassword(), user.getRole(), user.getIsActive());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        mockMvc.perform(post("/like/{recipeId}", recipeId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        // Try to like the same recipe again
        mockMvc.perform(post("/like/{recipeId}", recipeId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("error-page"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void testLikeWithoutAuthentication_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(post("/like/{recipeId}", recipeId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
