package app.recipe;

import app.category.model.Category;
import app.category.model.CategoryName;
import app.category.service.CategoryService;
import app.cloudinary.CloudinaryService;
import app.exception.RecipeNotFoundException;
import app.recipe.model.Recipe;
import app.recipe.repository.RecipeRepository;
import app.recipe.service.RecipeService;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private UserService userService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private RecipeService recipeService;

    private UUID recipeId;
    private Recipe recipe;
    private User user;

    @BeforeEach
    void setUp() {
        recipeId = UUID.randomUUID();
        user = new User();
        user.setId(UUID.randomUUID());

        recipe = Recipe.builder()
                .id(recipeId)
                .title("Test Recipe")
                .description("A delicious test recipe")
                .ingredients(List.of("Salt", "Pepper"))
                .instructions("Mix and cook")
                .createdDate(LocalDateTime.now())
                .createdBy(user)
                .image("test-image-url")
                .servings(2)
                .cookTime(30)
                .prepTime(10)
                .categories(new ArrayList<>())
                .likes(new ArrayList<>())
                .comments(new ArrayList<>())
                .build();
    }

    @Test
    void testGetAllRecipes() {
        Pageable pageable = PageRequest.of(0, 5);

        List<Recipe> recipes = List.of(recipe);

        Page<Recipe> recipePage = new PageImpl<>(recipes, pageable, recipes.size());

        when(recipeRepository.findAll(pageable)).thenReturn(recipePage);

        Page<RecipeShortInfo> result = recipeService.getAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Recipe", result.getContent().get(0).getTitle());

        verify(recipeRepository).findAll(pageable);
    }


    @Test
    void testGetDetailsById() {
        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));

        RecipeDetails recipeDetails = recipeService.getDetailsById(recipeId);

        assertNotNull(recipeDetails);
        assertEquals(recipeId, recipeDetails.getId());
        assertEquals("Test Recipe", recipeDetails.getTitle());
        assertEquals("A delicious test recipe", recipeDetails.getDescription());
        assertEquals("Salt, Pepper", String.join(", ", recipeDetails.getIngredients()));
        assertEquals("Mix and cook", recipeDetails.getInstructions());
        assertEquals(30, recipeDetails.getCookTime());
        assertEquals(10, recipeDetails.getPrepTime());
        assertEquals(user, recipeDetails.getCreatedBy());
        assertEquals(2, recipeDetails.getServings());
        assertEquals("test-image-url", recipeDetails.getImage());
        assertEquals(0, recipeDetails.getComments().size());
        assertEquals(0, recipeDetails.getLikes());

        verify(recipeRepository, times(1)).findById(recipeId);
    }

    @Test
    void testGetRecipesByCreator() {
        List<Recipe> expectedRecipes = List.of(recipe);
        UUID userId = UUID.randomUUID();

        when(userService.getUserById(userId)).thenReturn(user);
        when(recipeRepository.findAllByCreatedBy(user)).thenReturn(expectedRecipes);

        List<Recipe> result = recipeService.getRecipesByCreator(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Recipe", result.get(0).getTitle());

        verify(userService).getUserById(userId);
        verify(recipeRepository).findAllByCreatedBy(user);
    }

    @Test
    void testGetAddRecipeById() {
        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));

        EditRecipe result = recipeService.getAddRecipeById(recipeId);

        assertNotNull(result);
        assertEquals(recipeId, result.getId());
        assertEquals("Test Recipe", result.getTitle());
        assertEquals("A delicious test recipe", result.getDescription());
        assertEquals("Salt,Pepper", result.getIngredients());
        assertEquals("Mix and cook", result.getInstructions());
        assertEquals(2, result.getServings());
        assertEquals(30, result.getCookTime());
        assertEquals(10, result.getPrepTime());

        verify(recipeRepository).findById(recipeId);
    }

    @Test
    void testUpdateRecipe() {
        Category category = Category.builder()
                .id(UUID.randomUUID())
                .name(CategoryName.MAIN_COURSE)
                .recipes(List.of(recipe))
                .build();

        recipe.setCategories(List.of(Category.builder().name(CategoryName.DESSERTS).build()));
        recipe.setIngredients(List.of("Salt", "Pepper"));
        recipe.setInstructions("Old Instructions");

        MockMultipartFile img = new MockMultipartFile("image", "new-image-url", "image/jpeg", new byte[]{1, 2, 3});

        EditRecipe updatedRecipe = EditRecipe.builder()
                .id(recipeId)
                .title("New Title")
                .description("New Description")
                .categories(List.of(CategoryName.MAIN_COURSE))
                .ingredients("Salt,Pepper,Sugar")
                .instructions("New Instructions")
                .servings(4)
                .cookTime(45)
                .prepTime(15)
                .image(img)
                .build();

        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));
        when(categoryService.getByName(CategoryName.MAIN_COURSE)).thenReturn(category);
        when(cloudinaryService.uploadImage(any())).thenReturn("https://mock-image-url.com/image.jpg");

        recipeService.update(updatedRecipe);

        assertEquals("New Title", recipe.getTitle());
        assertEquals("New Description", recipe.getDescription());
        assertEquals(List.of("Salt", "Pepper", "Sugar"), recipe.getIngredients());
        assertEquals("New Instructions", recipe.getInstructions());
        assertEquals(4, recipe.getServings());
        assertEquals(45, recipe.getCookTime());
        assertEquals(15, recipe.getPrepTime());
        assertEquals("https://mock-image-url.com/image.jpg", recipe.getImage());

        // Verify repository interactions
        verify(recipeRepository).save(recipe);
        verify(categoryService).getByName(CategoryName.MAIN_COURSE);
        verify(cloudinaryService).uploadImage(any());
    }

    @Test
    void getByIdShouldReturnRecipeWhenRecipeExists() {
        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));

        Recipe foundRecipe = recipeService.getById(recipeId);

        assertNotNull(foundRecipe);
        assertEquals("Test Recipe", foundRecipe.getTitle());
        verify(recipeRepository, times(1)).findById(recipeId);
    }

    @Test
    void getByIdShouldThrowExceptionWhenRecipeDoesNotExist() {
        when(recipeRepository.findById(recipeId)).thenReturn(Optional.empty());

        assertThrows(RecipeNotFoundException.class, () -> recipeService.getById(recipeId));
    }

    @Test
    void createShouldSaveRecipeSuccessfully() {
        AddRecipe addRecipe = new AddRecipe();
        addRecipe.setTitle("New Recipe");
        addRecipe.setDescription("A new delicious recipe");
        addRecipe.setIngredients("Salt, Sugar");
        addRecipe.setInstructions("Mix well and cook");
        addRecipe.setCategories(List.of(CategoryName.MAIN_COURSE));
        addRecipe.setServings(4);
        addRecipe.setCookTime(45);
        addRecipe.setPrepTime(15);

        when(userService.getUserById(user.getId())).thenReturn(user);
        when(categoryService.getByName(CategoryName.MAIN_COURSE)).thenReturn(Category.builder().name(CategoryName.MAIN_COURSE).build());
        when(recipeRepository.save(any(Recipe.class))).thenReturn(recipe);
        when(cloudinaryService.uploadImage(any())).thenReturn("https://mock-image-url.com/image.jpg");

        Recipe createdRecipe = recipeService.create(addRecipe, user.getId());

        String actualIngredients = String.join(", ", createdRecipe.getIngredients());

        assertNotNull(createdRecipe);
        assertEquals("Test Recipe", createdRecipe.getTitle());
        assertEquals("A delicious test recipe", createdRecipe.getDescription());
        assertEquals("Salt, Pepper", actualIngredients);
        assertEquals("Mix and cook", createdRecipe.getInstructions());
        assertEquals(30, createdRecipe.getCookTime());
        assertEquals(10, createdRecipe.getPrepTime());
        assertEquals(2, createdRecipe.getServings());

        verify(recipeRepository, times(1)).save(any(Recipe.class));
    }

    @Test
    void deleteShouldRemoveRecipeWhenExists() {
        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));

        recipeService.delete(recipeId);

        verify(recipeRepository, times(1)).delete(recipe);
    }

    @Test
    void testGetAllForAdmin() {
        Recipe recipe2 = new Recipe();
        recipe2.setId(UUID.randomUUID());
        recipe2.setTitle("Recipe 2");
        recipe2.setDescription("Description 2");
        recipe2.setCreatedBy(user);
        recipe2.setCreatedDate(LocalDateTime.now());

        List<Recipe> recipeList = List.of(recipe, recipe2);

        when(recipeRepository.findAll()).thenReturn(recipeList);

        List<RecipeForAdminPageInfo> result = recipeService.getAllForAdmin();

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(recipe.getId(), result.get(0).getId());
        assertEquals(recipe.getTitle(), result.get(0).getTitle());
        assertEquals(recipe.getCreatedBy().getUsername(), result.get(0).getAuthor());
        assertEquals(recipe.getCreatedBy().getUsername(), result.get(0).getAuthor());
        assertEquals(recipe.getCreatedDate(), result.get(0).getCreatedDate());

        assertEquals(recipe2.getId(), result.get(1).getId());
        assertEquals(recipe2.getTitle(), result.get(1).getTitle());
        assertEquals(recipe2.getCreatedBy().getUsername(), result.get(1).getAuthor());
        assertEquals(recipe2.getCreatedBy().getUsername(), result.get(1).getAuthor());
        assertEquals(recipe2.getCreatedDate(), result.get(1).getCreatedDate());

        verify(recipeRepository, times(1)).findAll();
    }

    @Test
    void testSearchRecipes() {
        String query = "pasta";
        Pageable pageable = PageRequest.of(0, 2);

        Recipe recipe2 = new Recipe();
        recipe2.setId(UUID.randomUUID());
        recipe2.setTitle("Pasta Alfredo");
        recipe2.setDescription("Cheesy and creamy.");
        recipe2.setCookTime(2);
        recipe2.setPrepTime(10);
        recipe2.setServings(1);

        List<Recipe> recipeList = List.of(recipe, recipe2);
        Page<Recipe> recipePage = new PageImpl<>(recipeList, pageable, recipeList.size());

        when(recipeRepository.findAllByTitleContainingIgnoreCase(query, pageable)).thenReturn(recipePage);

        Page<RecipeShortInfo> result = recipeService.searchRecipes(query, pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());

        assertEquals(recipe.getId(), result.getContent().get(0).getId());
        assertEquals(recipe.getTitle(), result.getContent().get(0).getTitle());

        assertEquals(recipe2.getId(), result.getContent().get(1).getId());
        assertEquals(recipe2.getTitle(), result.getContent().get(1).getTitle());

        verify(recipeRepository, times(1)).findAllByTitleContainingIgnoreCase(query, pageable);
    }

    @Test
    void testGetRecipesByIds() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        List<UUID> favoriteRecipeIds = List.of(id1, id2);

        Recipe recipe1 = new Recipe();
        recipe1.setId(id1);
        recipe1.setTitle("Spaghetti Bolognese");

        Recipe recipe2 = new Recipe();
        recipe2.setId(id2);
        recipe2.setTitle("Chicken Curry");

        List<Recipe> expectedRecipes = List.of(recipe1, recipe2);

        when(recipeRepository.findAllByIdIn(favoriteRecipeIds)).thenReturn(expectedRecipes);

        List<Recipe> result = recipeService.getRecipesByIds(favoriteRecipeIds);

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(id1, result.get(0).getId());
        assertEquals("Spaghetti Bolognese", result.get(0).getTitle());

        assertEquals(id2, result.get(1).getId());
        assertEquals("Chicken Curry", result.get(1).getTitle());

        verify(recipeRepository, times(1)).findAllByIdIn(favoriteRecipeIds);
    }
}