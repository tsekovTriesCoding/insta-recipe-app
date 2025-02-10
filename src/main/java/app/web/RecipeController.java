package app.web;

import app.category.model.CategoryName;
import app.exception.RecipeNotFoundException;
import app.favorite.FavoriteServiceClient;
import app.like.service.LikeService;
import app.recipe.model.Recipe;
import app.recipe.service.RecipeService;
import app.user.service.UserService;
import app.web.dto.AddRecipe;
import app.web.dto.EditRecipe;
import app.web.dto.RecipeDetails;
import app.web.dto.RecipeShortInfo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
@RequestMapping("/recipes")
public class RecipeController {

    private final RecipeService recipeService;
    private final LikeService likeService;
    private final UserService userService;
    private final FavoriteServiceClient favoriteServiceClient;

    @ModelAttribute(name = "categories")
    public CategoryName[] categoryName() {
        return CategoryName.values();
    }

    @GetMapping("/all")
    public String allRecipes(@RequestParam(value = "query", required = false) String query,
                             Model model,
                             @PageableDefault(size = 3) Pageable pageable) {

        Page<RecipeShortInfo> recipes;
        if (query != null && !query.trim().isEmpty()) {
            recipes = recipeService.searchRecipes(query, pageable);

            if (!recipes.hasContent()) {
                recipes = recipeService.getAll(pageable);
            }

            model.addAttribute("query", query);
        } else {
            recipes = recipeService.getAll(pageable);
        }

        model.addAttribute("recipes", recipes);

        return "recipes";
    }

    @GetMapping("/{id}")
    public String recipeDetails(@PathVariable UUID id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                Model model) {
        RecipeDetails recipe = recipeService.getDetailsById(id);
        boolean isCreator = recipe.getCreator().equals(userDetails.getUsername());
        boolean hasLiked = likeService.userHasLikedRecipe(userDetails.getUsername(), id);

        UUID userId = userService.getByUsername(userDetails.getUsername()).getId();

        List<UUID> favoriteRecipes = favoriteServiceClient.getFavoriteRecipeIds(userId);
        boolean isFavorite = favoriteRecipes.contains(id);

        model.addAttribute("recipe", recipe);
        model.addAttribute("isCreator", isCreator);
        model.addAttribute("hasLiked", hasLiked);
        model.addAttribute("isFavorite", isFavorite);

        return "recipe-details";
    }

    @GetMapping("/add")
    public String addRecipe(Model model) {
        model.addAttribute("addRecipe", new AddRecipe());

        return "add-recipe";
    }

    @PostMapping("/add")
    public String addRecipe(@Valid AddRecipe addRecipe,
                            BindingResult bindingResult,
                            @AuthenticationPrincipal UserDetails userDetails) {

        if (bindingResult.hasErrors()) {
            /*
            This is if the name of the object is different from that in the binding result:
             redirectAttributes.addFlashAttribute("recipe", recipe);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.recipe", bindingResult);
          */
            return "add-recipe";
        }

        Recipe newRecipe = recipeService.create(addRecipe, userDetails.getUsername());

        return "redirect:/recipes/" + newRecipe.getId();
    }

    @GetMapping("/edit/{id}")
    public String editRecipe(@PathVariable UUID id, Model model) {
        EditRecipe editRecipe = recipeService.getAddRecipeById(id);

        model.addAttribute("editRecipe", editRecipe);

        return "edit-recipe";
    }

    @PostMapping("/edit/{id}")
    public String editRecipe(@Valid EditRecipe editRecipe,
                             BindingResult bindingResult) throws IOException {

        if (bindingResult.hasErrors()) {
            return "edit-recipe";
        }

        recipeService.update(editRecipe);

        return "redirect:/recipes/" + editRecipe.getId();
    }

    @GetMapping("/my-recipes")
    public String myRecipes(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        List<Recipe> myRecipes = recipeService.getRecipesByCreator(userDetails.getUsername());

        model.addAttribute("myRecipes", myRecipes);

        return "my-recipes";
    }

    @DeleteMapping("/delete/{id}")
    public String deleteRecipe(@PathVariable UUID id) {
        recipeService.delete(id);

        return "redirect:/recipes/my-recipes";
    }

    @ExceptionHandler(RecipeNotFoundException.class)
    public String handleRecipeNotFound(RecipeNotFoundException ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        return "error-page";
    }
}