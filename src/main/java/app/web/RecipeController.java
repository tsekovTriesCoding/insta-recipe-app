package app.web;

import app.category.model.CategoryName;
import app.favorite.service.FavoriteService;
import app.like.service.LikeService;
import app.mapper.DtoMapper;
import app.recipe.model.Recipe;
import app.recipe.service.RecipeService;
import app.security.CustomUserDetails;
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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
@RequestMapping("/recipes")
public class RecipeController {

    private final RecipeService recipeService;
    private final LikeService likeService;
    private final FavoriteService favoriteService;

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
            recipes = recipeService.searchRecipes(query, pageable).map(DtoMapper::mapRecipeToRecipeShortInfo);

            if (!recipes.hasContent()) {
                recipes = recipeService.getAll(pageable).map(DtoMapper::mapRecipeToRecipeShortInfo);
            }

            model.addAttribute("query", query);
        } else {
            recipes = recipeService.getAll(pageable).map(DtoMapper::mapRecipeToRecipeShortInfo);
        }

        model.addAttribute("recipes", recipes);

        return "recipes";
    }

    @GetMapping("/{id}")
    public String recipeDetails(@PathVariable UUID id,
                                @AuthenticationPrincipal CustomUserDetails customUserDetails,
                                Model model) {
        RecipeDetails recipe = DtoMapper.mapRecipeToRecipeDetails(recipeService.getById(id));
        boolean isCreator = recipe.getCreator().equals(customUserDetails.getUsername());
        boolean hasLiked = likeService.userHasLikedRecipe(customUserDetails.getId(), id);
        boolean isFavorite = favoriteService.isFavorite(customUserDetails.getId(), recipe.getId());

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
                            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        if (bindingResult.hasErrors()) {
            /*
            This is if the name of the object is different from that in the binding result:
             redirectAttributes.addFlashAttribute("recipe", recipe);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.recipe", bindingResult);
          */
            return "add-recipe";
        }

        Recipe newRecipe = recipeService.create(addRecipe, customUserDetails.getId());

        return "redirect:/recipes/" + newRecipe.getId();
    }

    @GetMapping("/edit/{id}")
    public String editRecipe(@PathVariable UUID id, Model model) {
        EditRecipe editRecipe = DtoMapper.mapRecipeToEditRecipe(recipeService.getById(id));

        model.addAttribute("editRecipe", editRecipe);

        return "edit-recipe";
    }

    @PutMapping("/edit/{id}")
    public String editRecipe(@Valid EditRecipe editRecipe,
                             BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "edit-recipe";
        }

        recipeService.update(editRecipe);

        return "redirect:/recipes/" + editRecipe.getId();
    }

    @GetMapping("/my-recipes")
    public String myRecipes(@AuthenticationPrincipal CustomUserDetails customUserDetails, Model model) {
        List<Recipe> myRecipes = recipeService.getRecipesByCreator(customUserDetails.getId());

        model.addAttribute("myRecipes", myRecipes);

        return "my-recipes";
    }

    @DeleteMapping("/delete/{id}")
    public String deleteRecipe(@PathVariable UUID id) {
        recipeService.delete(id);

        return "redirect:/recipes/my-recipes";
    }
}