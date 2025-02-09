package app.recipe.service;

import app.category.model.Category;
import app.category.model.CategoryName;
import app.category.service.CategoryService;
import app.cloudinary.CloudinaryService;
import app.exception.RecipeNotFoundException;
import app.mapper.DtoMapper;
import app.recipe.model.Recipe;
import app.recipe.repository.RecipeRepository;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final CategoryService categoryService;
    private final UserService userService;
    private final CloudinaryService cloudinaryService;

    public List<RecipeShortInfo> getAll() {
        List<Recipe> recipes = recipeRepository.findAll();

        return recipes
                .stream()
                .map(DtoMapper::mapRecipeToRecipeShortInfo)
                .toList();
    }

    @Transactional(readOnly = true)
    public RecipeDetails getDetailsById(UUID recipeId) {
        Recipe recipe = getById(recipeId);

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
                .likes(recipe.getLikes().size())
                .build();
    }

    public Recipe getById(UUID recipeId) {
        return recipeRepository.findById(recipeId).
                orElseThrow(() -> new RecipeNotFoundException("Recipe with id " + recipeId + " not found."));
    }

    public Recipe create(AddRecipe recipe, String username) {
        User user = userService.getByUsername(username);

//        String title = recipe.getTitle();
//
//        if (recipeRepository.existsByTitle(title)) {
//            List<Recipe> allByTitle = recipeRepository.getAllByTitle(title);
//
//            title = title + allByTitle.size();
//        }
//
//        String imageUrl = saveImage(recipe.getImage(), title + "-recipe");

        String imageUrl = cloudinaryService.uploadImage(recipe.getImage());

        Recipe newRecipe = initializeRecipe(recipe, user, imageUrl);

        return recipeRepository.save(newRecipe);
    }

    private Recipe initializeRecipe(AddRecipe recipe, User creator, String imageUrl) {
        List<Category> categories = recipe.getCategories()
                .stream()
                .map(categoryService::getByName).toList();

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

    public List<Recipe> getRecipesByCreator(String username) {
        User user = userService.getByUsername(username);
        return recipeRepository.findAllByCreatedBy(user);
    }

    public EditRecipe getAddRecipeById(UUID id) {
        Recipe recipe = this.getById(id);

        List<CategoryName> categories = recipe.getCategories().stream().map(Category::getName).toList();
        String ingredients = String.join(",", recipe.getIngredients());

        return EditRecipe.builder()
                .id(id)
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .categories(categories)
                .ingredients(ingredients)
                .instructions(recipe.getInstructions())
                .servings(recipe.getServings())
                .cookTime(recipe.getCookTime())
                .prepTime(recipe.getPrepTime())
                .build();
    }

    @Transactional
    public void update(EditRecipe recipe) {
        Recipe recipeToUpdate = this.getById(recipe.getId());
        List<Category> categories = recipe.getCategories()
                .stream()
                .map(categoryService::getByName)
                .toList();

        if (!recipe.getTitle().equals(recipeToUpdate.getTitle())) {
            recipeToUpdate.setTitle(recipe.getTitle());
        }

        if (!recipe.getDescription().equals(recipeToUpdate.getDescription())) {
            recipeToUpdate.setDescription(recipe.getDescription());
        }

        if (!categories.equals(recipeToUpdate.getCategories())) {
            categories.forEach(category -> {
                categoryService.update(category, recipeToUpdate);
            });
        }

        if (!recipe.getIngredients().equals(String.join(",", recipeToUpdate.getIngredients()))) {
            recipeToUpdate.getIngredients().clear();
            recipeToUpdate.getIngredients().addAll(List.of(recipe.getIngredients().split(",")));
        }

        if (!recipe.getInstructions().equals(recipeToUpdate.getInstructions())) {
            recipeToUpdate.setInstructions(recipe.getInstructions());
        }

        if (!recipe.getServings().equals(recipeToUpdate.getServings())) {
            recipeToUpdate.setServings(recipe.getServings());
        }

        if (!recipe.getCookTime().equals(recipeToUpdate.getCookTime())) {
            recipeToUpdate.setCookTime(recipe.getCookTime());
        }

        if (recipe.getPrepTime() != null && !recipe.getPrepTime().equals(recipeToUpdate.getPrepTime())) {
            recipeToUpdate.setPrepTime(recipe.getPrepTime());
        }

        if (recipe.getImage() != null && !recipe.getImage().isEmpty()) {
            String imageUrl = cloudinaryService.uploadImage(recipe.getImage());
            recipeToUpdate.setImage(imageUrl);
        }

        recipeRepository.save(recipeToUpdate);
    }

    public void delete(UUID id) {
        //TODO: remove the picture from the uploads files...
        recipeRepository.delete(getById(id));
    }

    public List<RecipeForAdminPageInfo> getAllForAdmin() {
        return recipeRepository.findAll()
                .stream()
                .map(DtoMapper::mapRecipeToRecipeForAdminPageInfo)
                .toList();
    }
}