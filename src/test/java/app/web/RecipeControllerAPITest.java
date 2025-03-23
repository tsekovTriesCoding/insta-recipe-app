package app.web;

import app.category.model.CategoryName;
import app.exception.RecipeNotFoundException;
import app.favorite.service.FavoriteService;
import app.like.service.LikeService;
import app.recipe.model.Recipe;
import app.recipe.service.RecipeService;
import app.security.CustomUserDetails;
import app.user.model.Role;
import app.user.model.User;
import app.web.dto.AddRecipe;
import app.web.dto.EditRecipe;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static app.TestBuilder.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecipeController.class)
public class RecipeControllerAPITest {

    @MockitoBean
    private RecipeService recipeService;

    @MockitoBean
    private LikeService likeService;

    @MockitoBean
    private FavoriteService favoriteService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void testGetAllRecipes_whenDatabaseIsEmpty() throws Exception {
        Pageable pageable = PageRequest.of(0, 3);

        when(recipeService.getAll(pageable)).thenReturn(new PageImpl<>(new ArrayList<>()));

        mockMvc.perform(get("/recipes/all")
                        .param("page", "0")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(view().name("recipes"))
                .andExpect(model().attributeExists("recipes"))
                .andExpect(model().attribute("recipes", hasProperty("content", hasSize(0))))
                .andExpect(model().attribute("recipes", hasProperty("numberOfElements", is(0))));
    }

    @Test
    @WithMockUser
    void testGetAllRecipes_whenDatabaseHasRecipes() throws Exception {
        Pageable pageable = PageRequest.of(0, 3);

        Recipe recipe1 = Recipe.builder()
                .id(UUID.randomUUID())
                .title("title1")
                .description("description1")
                .cookTime(2)
                .servings(2)
                .build();

        Recipe recipe2 = Recipe.builder()
                .id(UUID.randomUUID())
                .title("title2")
                .description("description2")
                .cookTime(2)
                .servings(2)
                .build();

        Page<Recipe> recipePage = new PageImpl<>(List.of(recipe1, recipe2), pageable, 2);

        when(recipeService.getAll(pageable)).thenReturn(recipePage);

        mockMvc.perform(get("/recipes/all")
                        .param("page", "0")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(view().name("recipes"))
                .andExpect(model().attributeExists("recipes"))
                .andExpect(model().attribute("recipes", hasProperty("content", hasSize(Math.toIntExact(2L)))))
                .andExpect(model().attribute("recipes", hasProperty("totalElements", is(2L))));
    }

    @Test
    @WithMockUser
    void testSearchRecipes_withMatchingResults() throws Exception {
        Pageable pageable = PageRequest.of(0, 3);

        Recipe recipe1 = Recipe.builder()
                .id(UUID.randomUUID())
                .title("Pizza")
                .description("description1")
                .cookTime(2)
                .servings(2)
                .build();

        Page<Recipe> recipePage = new PageImpl<>(List.of(recipe1), pageable, 1);

        when(recipeService.searchRecipes("Pizza", pageable)).thenReturn(recipePage);

        mockMvc.perform(get("/recipes/all")
                        .param("query", "Pizza"))
                .andExpect(status().isOk())
                .andExpect(view().name("recipes"))
                .andExpect(model().attribute("recipes", hasProperty("content", hasSize(Math.toIntExact(1L)))))
                .andExpect(model().attribute("recipes", hasProperty("totalElements", is(1L))));
    }

    @Test
    @WithMockUser
    void testSearchRecipes_withNoMatchingResults_returnsAll() throws Exception {
        Pageable pageable = PageRequest.of(0, 3);

        Recipe recipe1 = Recipe.builder()
                .id(UUID.randomUUID())
                .title("Pizza")
                .description("description1")
                .cookTime(2)
                .servings(2)
                .build();

        Recipe recipe2 = Recipe.builder()
                .id(UUID.randomUUID())
                .title("Pizza")
                .description("description1")
                .cookTime(2)
                .servings(2)
                .build();

        Page<Recipe> recipePage = new PageImpl<>(List.of(recipe1,recipe2), pageable, 2);

        when(recipeService.searchRecipes("nothing", pageable)).thenReturn(new PageImpl<>(new ArrayList<>(), pageable, 0));
        when(recipeService.getAll(pageable)).thenReturn(recipePage);

        mockMvc.perform(get("/recipes/all").param("query", "nothing"))
                .andExpect(status().isOk())
                .andExpect(view().name("recipes"))
                .andExpect(model().attribute("recipes", hasProperty("content", hasSize(Math.toIntExact(2L)))))
                .andExpect(model().attribute("recipes", hasProperty("totalElements", is(2L))));
    }

    @Test
    @WithMockUser
    void testGetAllRecipes_whenPageDoesNotExist() throws Exception {
        Pageable pageable = PageRequest.of(1000, 3);

        when(recipeService.getAll(pageable)).thenReturn(new PageImpl<>(new ArrayList<>()));

        mockMvc.perform(get("/recipes/all")
                        .param("page", "1000")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("recipes"))
                .andExpect(model().attribute("recipes", hasProperty("content", hasSize(0))));
    }

    @Test
    void testRecipeDetails_whenRecipeExists() throws Exception {
        UUID recipeId = UUID.randomUUID();
        Recipe recipe = aRandomRecipe();
        recipe.setCreatedBy(User.builder().username("username").build());

        CustomUserDetails principal = new CustomUserDetails(UUID.randomUUID(), "username", "pass", Role.USER, true);

        when(recipeService.getById(recipeId)).thenReturn(recipe);

        mockMvc.perform(get("/recipes/" + recipeId)
                        .with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("recipe-details"))
                .andExpect(model().attributeExists("recipe"))
                .andExpect(model().attribute("recipe", hasProperty("title", is("title"))))
                .andExpect(model().attribute("recipe", hasProperty("description", is("description"))))
                .andExpect(model().attribute("recipe", hasProperty("ingredients", hasSize(2))))
                .andExpect(model().attribute("recipe", hasProperty("instructions", is("Instructions"))))
                .andExpect(model().attribute("isCreator", is(true)))
                .andExpect(model().attribute("hasLiked", is(false)))
                .andExpect(model().attribute("isFavorite", is(false)));
    }

    @Test
    void testRecipeDetails_whenUserIsNotCreator() throws Exception {
        UUID recipeId = UUID.randomUUID();
        Recipe recipe = aRandomRecipe();
        recipe.setCreatedBy(User.builder().username("username").build());

        CustomUserDetails principal = new CustomUserDetails(UUID.randomUUID(), "user", "pass", Role.USER, true);

        when(recipeService.getById(recipeId)).thenReturn(recipe);

        mockMvc.perform(get("/recipes/" + recipeId)
                        .with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("recipe-details"))
                .andExpect(model().attributeExists("recipe"))
                .andExpect(model().attribute("recipe", hasProperty("title", is("title"))))
                .andExpect(model().attribute("recipe", hasProperty("description", is("description"))))
                .andExpect(model().attribute("recipe", hasProperty("ingredients", hasSize(2))))
                .andExpect(model().attribute("recipe", hasProperty("instructions", is("Instructions"))))
                .andExpect(model().attribute("isCreator", is(false)))
                .andExpect(model().attribute("hasLiked", is(false)))
                .andExpect(model().attribute("isFavorite", is(false)));
    }

    @Test
    @WithMockUser
    void testRecipeDetails_whenRecipeDoesNotExist() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        when(recipeService.getById(nonExistentId)).thenThrow(RecipeNotFoundException.class);

        mockMvc.perform(get("/recipes/" + nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error-page"));
    }

    @Test
    @WithMockUser
    void testRecipeDetails_whenIdIsInvalid() throws Exception {
        mockMvc.perform(get("/recipes/invalid-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRecipeDetails_whenUserHasLikedRecipe() throws Exception {
        CustomUserDetails principal = new CustomUserDetails(UUID.randomUUID(), "user", "pass", Role.USER, true);

        Recipe recipe = aRandomRecipe();
        recipe.setCreatedBy(User.builder().username("username").build());

        when(recipeService.getById(recipe.getId())).thenReturn(recipe);
        when(likeService.userHasLikedRecipe(principal.getId(), recipe.getId())).thenReturn(true);

        mockMvc.perform(get("/recipes/" + recipe.getId())
                        .with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(model().attribute("hasLiked", is(true)));
    }

    @Test
    void testRecipeDetails_whenUserHasFavoritedRecipe() throws Exception {
        CustomUserDetails principal = new CustomUserDetails(UUID.randomUUID(), "user", "pass", Role.USER, true);

        Recipe recipe = aRandomRecipe();
        recipe.setCreatedBy(User.builder().username("username").build());

        when(recipeService.getById(recipe.getId())).thenReturn(recipe);
        when(favoriteService.isFavorite(principal.getId(), recipe.getId())).thenReturn(true);

        mockMvc.perform(get("/recipes/" + recipe.getId())
                        .with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(model().attribute("isCreator", is(false)))
                .andExpect(model().attribute("hasLiked", is(false)))
                .andExpect(model().attribute("isFavorite", is(true)));
    }

    @Test
    @WithMockUser
    void testAddRecipePage_whenUserIsAuthenticated() throws Exception {
        mockMvc.perform(get("/recipes/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-recipe"))
                .andExpect(model().attributeExists("addRecipe"))
                .andExpect(model().attribute("addRecipe", instanceOf(AddRecipe.class)));
    }

    @Test
    void testAddRecipePage_whenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(get("/recipes/add"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Disabled
    void testAddRecipe_whenValidRecipeIsProvided() throws Exception {
        CustomUserDetails principal = new CustomUserDetails(UUID.randomUUID(), "user", "pass", Role.USER, true);

        AddRecipe addRecipe = aRandomAddRecipe();

        mockMvc.perform(multipart("/recipes/add")
                        .file((MockMultipartFile) addRecipe.getImage())
                        .characterEncoding("UTF-8")
                        .with(user(principal))
                        .with(csrf())
                        .formField("title", addRecipe.getTitle())
                        .formField("description", addRecipe.getDescription())
                        .formField("ingredients", addRecipe.getIngredients())
                        .formField("instructions", addRecipe.getInstructions())
                        .formField("categories", String.valueOf(addRecipe.getCategories().get(0)))
                        .formField("cookTime", String.valueOf(addRecipe.getCookTime()))
                        .formField("servings", String.valueOf(addRecipe.getServings()))
                        .formField("prepTime", String.valueOf(addRecipe.getPrepTime())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/recipes/*")); // Check that the redirect URL is like "/recipe/{id}"
    }

    @Test
    void testAddRecipe_whenInvalidRecipeIsProvided() throws Exception {
        CustomUserDetails principal = new CustomUserDetails(UUID.randomUUID(), "user", "pass", Role.USER, true);

        mockMvc.perform(post("/recipes/add")
                        .with(user(principal))
                        .with(csrf())
                        // with no image
                        .formField("title", "") // Empty title (invalid)
                        .formField("description", "Short") // Too short description (invalid)
                        .formField("categories", String.valueOf(new ArrayList<>())) // No categories
                        .formField("ingredients", "") // Empty ingredients
                        .formField("instructions", "") // Empty instructions
                        .formField("cookTime", "-10") // Negative time (invalid)
                        .formField("servings", "0")) // Invalid servings
                .andExpect(status().isOk()) // Should return 200 (stay on the same page)
                .andExpect(view().name("add-recipe")) // Should not redirect, but stay on form page
                .andExpect(model().attributeHasFieldErrors("addRecipe", "title", "description", "image", "categories", "cookTime", "servings"));
    }

    @Test
    void testEditRecipe_WhenRecipeExists() throws Exception {
        CustomUserDetails principal = new CustomUserDetails(UUID.randomUUID(), "user", "pass", Role.USER, true);

        Recipe recipe = aRandomRecipe();
        recipe.setCategories(new ArrayList<>());

        when(recipeService.getById(recipe.getId())).thenReturn(recipe);

        mockMvc.perform(get("/recipes/edit/" + recipe.getId())
                        .with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-recipe"))
                .andExpect(model().attributeExists("editRecipe"))
                .andExpect(model().attribute("editRecipe", hasProperty("title", is("title"))))
                .andExpect(model().attribute("editRecipe", hasProperty("description", is("description"))))
                .andExpect(model().attribute("editRecipe", hasProperty("categories", is(emptyCollectionOf(CategoryName.class)))))
                .andExpect(model().attribute("editRecipe", hasProperty("ingredients", is("tomato,cucumber"))))
                .andExpect(model().attribute("editRecipe", hasProperty("instructions", is("Instructions"))))
                .andExpect(model().attribute("editRecipe", hasProperty("cookTime", is(2))))
                .andExpect(model().attribute("editRecipe", hasProperty("servings", is(4))));
    }

    @Test
    @WithMockUser
    void testEditRecipe_WhenRecipeDoesNotExist() throws Exception {
        UUID invalidId = UUID.randomUUID();

        when(recipeService.getById(invalidId)).thenThrow(new RecipeNotFoundException("Recipe with id " + invalidId + " not found."));

        mockMvc.perform(get("/recipes/edit/" + invalidId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @Disabled
    void testEditRecipe_WhenValidRecipeProvided() throws Exception {
        EditRecipe editRecipe = aRandomEditRecipe();
        MockMultipartFile image = new MockMultipartFile(
                "image", "image.jpg", "image/jpeg", "test image content".getBytes());

        mockMvc.perform(multipart(HttpMethod.PUT, "/recipes/edit/" + aRandomEditRecipe().getId())
                        .file(image)
                        .with(csrf())
                        .formField("title", editRecipe.getTitle())
                        .formField("description", editRecipe.getDescription())
                        .formField("ingredients", editRecipe.getIngredients())
                        .formField("instructions", editRecipe.getInstructions())
                        .formField("categories", String.valueOf(editRecipe.getCategories().get(0)))
                        .formField("cookTime", String.valueOf(editRecipe.getCookTime()))
                        .formField("servings", String.valueOf(editRecipe.getServings()))
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recipes/" + UUID.randomUUID()));

    }

    @Test
    @WithMockUser
    @Disabled
    void testEditRecipe_WhenInvalidRecipeProvided() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", new byte[(3 * 1024 * 1024) + 1]);

        EditRecipe editRecipe = EditRecipe.builder()
                .title("")
                .description("Short")
                .image(image)
                .categories(new ArrayList<>())
                .ingredients("")
                .instructions("")
                .cookTime(-5)
                .servings(0)
                .build();

        mockMvc.perform(post("/recipes/edit/" + UUID.randomUUID())
                        .with(csrf())
                        .flashAttr("editRecipe", editRecipe))
                .andExpect(status().isOk()) // Should return 200 (stay on the same page)
                .andExpect(view().name("edit-recipe")) // Should not redirect, but stay on form page
                .andExpect(model().attributeHasFieldErrors("editRecipe", "title", "description", "image", "categories", "cookTime", "servings"));
    }

    @Test
    void testMyRecipes_WhenUserHasRecipes() throws Exception {
        CustomUserDetails principal = new CustomUserDetails(UUID.randomUUID(), "user", "pass", Role.USER, true);

        Recipe recipe = aRandomRecipe();

        when(recipeService.getRecipesByCreator(principal.getId())).thenReturn(List.of(recipe));

        mockMvc.perform(get("/recipes/my-recipes")
                        .with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("my-recipes"))
                .andExpect(model().attributeExists("myRecipes"))
                .andExpect(model().attribute("myRecipes", hasSize(1)))
                .andExpect(model().attribute("myRecipes", hasItem(hasProperty("title", is("title")))));

    }

    @Test
    void testMyRecipes_WhenUserHasNoRecipes() throws Exception {
        CustomUserDetails principal = new CustomUserDetails(UUID.randomUUID(), "user", "pass", Role.USER, true);

        mockMvc.perform(get("/recipes/my-recipes")
                        .with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("my-recipes"))
                .andExpect(model().attributeExists("myRecipes"))
                .andExpect(model().attribute("myRecipes", hasSize(0)));
    }

    @Test
    void testMyRecipes_WhenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(get("/recipes/my-recipes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void testDeleteRecipe_WhenRecipeExists() throws Exception {
        mockMvc.perform(delete("/recipes/delete/" + UUID.randomUUID())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recipes/my-recipes"));
    }

    @Test
    @WithMockUser
    void testDeleteRecipe_WhenRecipeDoesNotExist() throws Exception {
        UUID recipeId = UUID.randomUUID();

        doThrow(new RecipeNotFoundException("Recipe with id " + recipeId + " not found.")).when(recipeService).delete(recipeId);

        mockMvc.perform(delete("/recipes/delete/" + recipeId)
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
}
