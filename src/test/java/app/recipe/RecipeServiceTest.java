package app.recipe;

import app.activitylog.event.ActivityLogEvent;
import app.category.model.Category;
import app.category.model.CategoryName;
import app.category.service.CategoryService;
import app.cloudinary.dto.ImageUploadResult;
import app.cloudinary.service.CloudinaryService;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

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

    @Mock
    private ApplicationEventPublisher eventPublisher;

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
                .imagePublicId("test-image-public-id")
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

        Page<Recipe> result = recipeService.getAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Recipe", result.getContent().get(0).getTitle());

        verify(recipeRepository).findAll(pageable);
    }

    @Test
    void testGetDetailsById() {
        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));

        Recipe recipeById = recipeService.getById(recipeId);

        assertNotNull(recipeById);
        assertEquals(recipeId, recipeById.getId());
        assertEquals("Test Recipe", recipeById.getTitle());
        assertEquals("A delicious test recipe", recipeById.getDescription());
        assertEquals("Salt, Pepper", String.join(", ", recipeById.getIngredients()));
        assertEquals("Mix and cook", recipeById.getInstructions());
        assertEquals(30, recipeById.getCookTime());
        assertEquals(10, recipeById.getPrepTime());
        assertEquals(user, recipeById.getCreatedBy());
        assertEquals(2, recipeById.getServings());
        assertEquals("test-image-url", recipeById.getImage());
        assertEquals(0, recipeById.getComments().size());
        assertEquals(0, recipeById.getLikes().size());

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
    void testUpdateRecipe() {
        EditRecipe editRecipe = EditRecipe.builder()
                .id(recipeId)
                .title("New Title")
                .description("New Description")
                .categories(List.of(CategoryName.MAIN_COURSE))
                .ingredients("Salt,Pepper,Sugar")
                .instructions("New Instructions")
                .image(mock(MultipartFile.class))
                .servings(4)
                .cookTime(45)
                .prepTime(15)
                .build();

        ImageUploadResult uploadResult = new ImageUploadResult("image-url", "public-id");

        when(recipeRepository.save(any(Recipe.class))).thenReturn(recipe);
        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));
        when(cloudinaryService.uploadImage(any(MultipartFile.class))).thenReturn(uploadResult);
        when(categoryService.getByName(CategoryName.MAIN_COURSE)).thenReturn(Category.builder()
                .name(CategoryName.MAIN_COURSE).build());

        recipeService.update(editRecipe);

        Recipe updatedRecipe = recipeRepository.findById(recipe.getId()).orElseThrow();

        verify(recipeRepository).save(recipe);
        verify(categoryService).getByName(CategoryName.MAIN_COURSE);
        verify(cloudinaryService).uploadImage(any());

        //TODO: test the event action
        assertEquals("New Title", updatedRecipe.getTitle());
        assertEquals("New Description", updatedRecipe.getDescription());
        assertEquals(List.of(CategoryName.MAIN_COURSE), updatedRecipe.getCategories().stream().map(Category::getName).toList());
        assertEquals(List.of("Salt", "Pepper", "Sugar"), updatedRecipe.getIngredients());
        assertEquals("New Instructions", updatedRecipe.getInstructions());
        assertEquals(4, updatedRecipe.getServings());
        assertEquals(45, updatedRecipe.getCookTime());
        assertEquals(15, updatedRecipe.getPrepTime());
        assertEquals("image-url", updatedRecipe.getImage());
        assertEquals("public-id", updatedRecipe.getImagePublicId());
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
        addRecipe.setImage(mock(MultipartFile.class));
        addRecipe.setServings(4);
        addRecipe.setCookTime(45);
        addRecipe.setPrepTime(15);

        ImageUploadResult uploadResult = new ImageUploadResult("image-url", "public-id");

        when(userService.getUserById(user.getId())).thenReturn(user);
        when(recipeRepository.save(any(Recipe.class))).thenReturn(recipe);
        when(cloudinaryService.uploadImage(any(MultipartFile.class))).thenReturn(uploadResult);

        Recipe createdRecipe = recipeService.create(addRecipe, user.getId());

        ArgumentCaptor<ActivityLogEvent> eventCaptor = ArgumentCaptor.forClass(ActivityLogEvent.class);

        String actualIngredients = String.join(", ", createdRecipe.getIngredients());

        verify(recipeRepository, times(1)).save(any(Recipe.class));
        verify(eventPublisher, Mockito.times(1)).publishEvent(eventCaptor.capture());

        ActivityLogEvent capturedEvent = eventCaptor.getValue();
        String expectedAction = "You have successfully added recipe: " + createdRecipe.getTitle();

        assertNotNull(createdRecipe);
        assertEquals("Test Recipe", createdRecipe.getTitle());
        assertEquals("A delicious test recipe", createdRecipe.getDescription());
        assertEquals("Salt, Pepper", actualIngredients);
        assertEquals("Mix and cook", createdRecipe.getInstructions());
        assertEquals("test-image-url", createdRecipe.getImage());
        assertEquals("test-image-public-id", createdRecipe.getImagePublicId());
        assertEquals(30, createdRecipe.getCookTime());
        assertEquals(10, createdRecipe.getPrepTime());
        assertEquals(2, createdRecipe.getServings());

        assertEquals(user.getId(), capturedEvent.getUserId());
        assertEquals(expectedAction, capturedEvent.getAction());
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

        List<Recipe> result = recipeService.getAllForAdmin();

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(recipe.getId(), result.get(0).getId());
        assertEquals(recipe.getTitle(), result.get(0).getTitle());
        assertEquals(recipe.getCreatedBy().getUsername(), result.get(0).getCreatedBy().getUsername());
        assertEquals(recipe.getCreatedDate(), result.get(0).getCreatedDate());

        assertEquals(recipe2.getId(), result.get(1).getId());
        assertEquals(recipe2.getTitle(), result.get(1).getTitle());
        assertEquals(recipe2.getCreatedBy().getUsername(), result.get(1).getCreatedBy().getUsername());
        assertEquals(recipe2.getCreatedBy().getUsername(), result.get(1).getCreatedBy().getUsername());
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

        Page<Recipe> result = recipeService.searchRecipes(query, pageable);

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