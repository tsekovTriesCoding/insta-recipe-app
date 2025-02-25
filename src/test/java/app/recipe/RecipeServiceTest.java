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
import app.web.dto.AddRecipe;
import app.web.dto.RecipeDetails;
import app.web.dto.RecipeShortInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
}
