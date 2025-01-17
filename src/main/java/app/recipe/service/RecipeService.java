package app.recipe.service;

import app.recipe.model.Recipe;
import app.recipe.repository.RecipeRepository;
import app.web.dto.RecipeDetails;
import app.web.dto.RecipeShortInfo;
import lombok.RequiredArgsConstructor;
import org.hibernate.collection.spi.PersistentBag;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;

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
}
