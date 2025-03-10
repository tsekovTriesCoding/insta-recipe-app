package app.recipe.service;

import app.activitylog.event.ActivityLogEvent;
import app.category.model.Category;
import app.category.service.CategoryService;
import app.cloudinary.dto.ImageUploadResult;
import app.cloudinary.service.CloudinaryService;
import app.exception.RecipeNotFoundException;
import app.recipe.model.Recipe;
import app.recipe.repository.RecipeRepository;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.AddRecipe;
import app.web.dto.EditRecipe;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final CategoryService categoryService;
    private final UserService userService;
    private final CloudinaryService cloudinaryService;
    private final ApplicationEventPublisher eventPublisher;

    public Page<Recipe> getAll(Pageable pageable) {
        return recipeRepository.findAll(pageable);
    }

    public Recipe getById(UUID recipeId) {
        return recipeRepository.findById(recipeId).
                orElseThrow(() -> new RecipeNotFoundException("Recipe with id " + recipeId + " not found."));
    }

    public Recipe create(AddRecipe addRecipe, UUID id) {
        User user = userService.getUserById(id);

        // Upload new image
        ImageUploadResult uploadResult = cloudinaryService.uploadImage(addRecipe.getImage());
        Recipe newRecipe = initializeRecipe(addRecipe, user, uploadResult.getImageUrl());
        newRecipe.setImagePublicId(uploadResult.getPublicId());

        Recipe recipe = recipeRepository.save(newRecipe);

        eventPublisher.publishEvent(new ActivityLogEvent(user.getId(), "You have successfully added recipe: " + recipe.getTitle()));

        return recipe;
    }

    private Recipe initializeRecipe(AddRecipe recipe, User creator, String imageUrl) {
        List<Category> categories = recipe.getCategories()
                .stream()
                .map(categoryService::getByName)
                .toList();

        List<String> ingredients = Arrays.stream(recipe.getIngredients().split(",")).toList();

        return Recipe.builder()
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .categories(categories)
                .ingredients(ingredients)
                .instructions(recipe.getInstructions())
                .createdDate(LocalDateTime.now())
                .createdBy(creator)
                .servings(recipe.getServings())
                .cookTime(recipe.getCookTime())
                .prepTime(recipe.getPrepTime())
                .image(imageUrl)
                .build();
    }

    public List<Recipe> getRecipesByCreator(UUID id) {
        User user = userService.getUserById(id);
        return recipeRepository.findAllByCreatedBy(user);
    }

    @Transactional
    public void update(EditRecipe editRecipe) {
        Recipe recipeToUpdate = getById(editRecipe.getId());
        List<Category> categories = editRecipe.getCategories()
                .stream()
                .map(categoryService::getByName)
                .collect(Collectors.toList());

        if (!editRecipe.getTitle().equals(recipeToUpdate.getTitle())) {
            recipeToUpdate.setTitle(editRecipe.getTitle());
        }

        if (!editRecipe.getDescription().equals(recipeToUpdate.getDescription())) {
            recipeToUpdate.setDescription(editRecipe.getDescription());
        }

        if (!categories.equals(recipeToUpdate.getCategories())) {
            recipeToUpdate.setCategories(categories);
        }

        if (!editRecipe.getIngredients().equals(String.join(",", recipeToUpdate.getIngredients()))) {
            List<String> updatedIngredients = new ArrayList<>(List.of(editRecipe.getIngredients().split(",")));
            recipeToUpdate.setIngredients(updatedIngredients);
        }

        if (!editRecipe.getInstructions().equals(recipeToUpdate.getInstructions())) {
            recipeToUpdate.setInstructions(editRecipe.getInstructions());
        }

        if (!editRecipe.getServings().equals(recipeToUpdate.getServings())) {
            recipeToUpdate.setServings(editRecipe.getServings());
        }

        if (!editRecipe.getCookTime().equals(recipeToUpdate.getCookTime())) {
            recipeToUpdate.setCookTime(editRecipe.getCookTime());
        }

        if (editRecipe.getPrepTime() != null && !editRecipe.getPrepTime().equals(recipeToUpdate.getPrepTime())) {
            recipeToUpdate.setPrepTime(editRecipe.getPrepTime());
        }

        if (editRecipe.getImage() != null && !editRecipe.getImage().isEmpty()) {
            // Delete old image from Cloudinary
            cloudinaryService.deleteImage(recipeToUpdate.getImagePublicId());
            // Upload new image
            ImageUploadResult uploadResult = cloudinaryService.uploadImage(editRecipe.getImage());
            recipeToUpdate.setImage(uploadResult.getImageUrl());
            recipeToUpdate.setImagePublicId(uploadResult.getPublicId());
        }

        Recipe recipe = recipeRepository.save(recipeToUpdate);

        eventPublisher.publishEvent(new ActivityLogEvent(recipe.getCreatedBy().getId(),
                "You have successfully updated recipe: " + recipe.getTitle()));
    }

    public void delete(UUID id) {
        Recipe recipe = getById(id);

        cloudinaryService.deleteImage(recipe.getImagePublicId());

        recipeRepository.delete(recipe);

        eventPublisher.publishEvent(new ActivityLogEvent(recipe.getCreatedBy().getId(),
                "You have successfully deleted recipe: " + recipe.getTitle()));
    }

    public List<Recipe> getAllForAdmin() {
        return recipeRepository.findAll();
    }

    public Page<Recipe> searchRecipes(String query, Pageable pageable) {
        return recipeRepository.findAllByTitleContainingIgnoreCase(query, pageable);
    }

    public List<Recipe> getRecipesByIds(List<UUID> favoriteRecipeIds) {
        return recipeRepository.findAllByIdIn(favoriteRecipeIds);
    }
}