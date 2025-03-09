package app.category;

import app.category.model.Category;
import app.category.model.CategoryName;
import app.category.repository.CategoryRepository;
import app.category.service.CategoryService;
import app.exception.CategoryNotFoundException;
import app.recipe.model.Recipe;
import app.web.dto.CategoryDetails;
import app.web.dto.CategoryShort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void getAllShouldReturnCategoryShort() {
        // Arrange
        Category category1 = Category.builder()
                .id(UUID.fromString("5ec33a5a-8f51-4f29-a7af-1294573da68f"))
                .name(CategoryName.BEVERAGES)
                .build();

        Category category2 = Category.builder()
                .id(UUID.fromString("2938bf5e-b39f-41f4-a731-830b5eac6566"))
                .name(CategoryName.VEGAN)
                .build();

        List<Category> mockCategories = Arrays.asList(category1, category2);
        when(categoryRepository.findAll()).thenReturn(mockCategories);

        CategoryShort categoryShort1 = CategoryShort.builder()
                .id(UUID.fromString("5ec33a5a-8f51-4f29-a7af-1294573da68f"))
                .name(CategoryName.BEVERAGES)
                .build();

        CategoryShort categoryShort2 = CategoryShort.builder()
                .id(UUID.fromString("2938bf5e-b39f-41f4-a731-830b5eac6566"))
                .name(CategoryName.VEGAN)
                .build();

        // Act
        List<Category> result = categoryService.getAll();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Category::getId)
                .containsExactly(categoryShort1.getId(), categoryShort2.getId());
        assertThat(result).extracting(Category::getName)
                .containsExactly(categoryShort1.getName(), categoryShort2.getName());

        // Verify that repository.findAll() was called once
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    void getByIdShouldReturnCategoryDetails() {
        //Arrange
        UUID categoryId = UUID.randomUUID();
        Category category = Category.builder()
                .id(categoryId)
                .name(CategoryName.BEVERAGES)
                .description("Random description")
                .recipes(List.of())
                .build();

        CategoryDetails categoryDetails = CategoryDetails
                .builder()
                .id(categoryId)
                .name(category.getName().getValue())
                .description("Random description")
                .recipes(List.of())
                .build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        // Act: Call the service method
        Category result = categoryService.getById(categoryId);

        // Assert: Verify mapping works correctly
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(categoryId);
        assertThat(result.getName()).isEqualTo(CategoryName.BEVERAGES.getValue());
        assertThat(result.getDescription()).isEqualTo("Random description");
        assertThat(result.getRecipes()).isEqualTo(List.of());

        verify(categoryRepository, times(1)).findById(categoryId);
    }

    @Test
    void getByIdShouldThrowExceptionWhenCategoryNotFound() {
        UUID categoryId = UUID.randomUUID();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getById(categoryId))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessage("Category with id " + categoryId + " not found");

        verify(categoryRepository, times(1)).findById(categoryId);
    }

    @Test
    void getByNameShouldReturnCategory() {
        UUID categoryId = UUID.randomUUID();
        CategoryName categoryName = CategoryName.BEVERAGES;

        Category category = Category.builder()
                .id(categoryId)
                .name(categoryName)
                .description("Random description")
                .build();

        when(categoryRepository.findByName(categoryName)).thenReturn(Optional.of(category));

        Category result = categoryService.getByName(CategoryName.BEVERAGES);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(categoryId);
        assertThat(result.getName()).isEqualTo(CategoryName.BEVERAGES);
        assertThat(result.getDescription()).isEqualTo("Random description");

        verify(categoryRepository, times(1)).findByName(categoryName);
    }

    @Test
    void getByNameShouldThrowExceptionWhenCategoryNotFound() {
        CategoryName categoryName = CategoryName.BEVERAGES;
        when(categoryRepository.findByName(categoryName)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getByName(categoryName))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessage("Category with name " + categoryName + " not found");

        verify(categoryRepository, times(1)).findByName(categoryName);
    }

    @Test
    void updateShouldAddRecipeWhenNotAlreadyInCategory() {
        Recipe recipe = Recipe.builder()
                .id(UUID.randomUUID())
                .title("Random title")
                .categories(new ArrayList<>())
                .build();

        Category category = Category.builder()
                .id(UUID.randomUUID())
                .name(CategoryName.BEVERAGES)
                .recipes(new ArrayList<>())
                .build();

        categoryService.update(category, recipe);

        assertThat(category.getRecipes()).contains(recipe);
        assertThat(recipe.getCategories()).contains(category);

        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    void updateShouldRemoveRecipeWhenAlreadyInCategory() {
        Recipe recipe = Recipe.builder()
                .id(UUID.randomUUID())
                .title("Random title")
                .categories(new ArrayList<>())
                .build();

        Category category = Category.builder()
                .id(UUID.randomUUID())
                .name(CategoryName.BEVERAGES)
                .recipes(new ArrayList<>())
                .build();

        category.getRecipes().add(recipe);
        recipe.getCategories().add(category);

        categoryService.update(category, recipe);

        assertThat(category.getRecipes()).doesNotContain(recipe);
        assertThat(recipe.getCategories()).doesNotContain(category);

        verify(categoryRepository, times(1)).save(category);
    }
}