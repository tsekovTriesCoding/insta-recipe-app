package app.recipe.service;

import app.category.model.Category;
import app.category.repository.CategoryRepository;
import app.recipe.model.Recipe;
import app.recipe.repository.RecipeRepository;
import app.user.model.User;
import app.user.repository.UserRepository;
import app.web.dto.AddRecipe;
import app.web.dto.RecipeDetails;
import app.web.dto.RecipeShortInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public List<RecipeShortInfo> getAll() {
        List<Recipe> recipes = recipeRepository.findAll();

        return recipes
                .stream()
                .map(this::map)
                .toList();
    }

    private RecipeShortInfo map(Recipe recipe) {
        return RecipeShortInfo.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .cookTime(recipe.getCookTime())
                .servings(recipe.getServings())
                .build();
    }

    @Transactional(readOnly = true)
    public RecipeDetails getById(UUID recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId).orElseThrow(NoSuchElementException::new);

        return RecipeDetails.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .ingredients(recipe.getIngredients())
                .instructions(recipe.getInstructions())
                .createdDate(recipe.getCreatedDate())
                .cookTime(recipe.getCookTime())
                .prepTime(recipe.getPrepTime())
                .createdBy(recipe.getCreatedBy())
                .servings(recipe.getServings())
                .image(recipe.getImage())
                .comments(recipe.getComments())
                .likes(recipe.getLikes())
                .build();
    }

    public Recipe create(AddRecipe recipe, String username) {
        User user = userRepository.findByUsername(username).orElseThrow(NoSuchElementException::new);

        List<Category> categories = recipe.getCategories()
                .stream()
                .map(categoryName -> categoryRepository.findByName(categoryName)
                        .orElseThrow(NoSuchElementException::new)).toList();

        List<String> ingredients = Arrays.stream(recipe.getIngredients().split(",")).toList();

        Recipe newRecipe = Recipe.builder()
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .categories(categories)
                .ingredients(ingredients)
                .instructions(recipe.getInstructions())
                .createdDate(LocalDateTime.now())
                .createdBy(user)
                .servings(5)
                .cookTime(2)
                .build();

        return recipeRepository.save(newRecipe);
    }
}
