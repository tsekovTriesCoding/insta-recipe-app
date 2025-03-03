package app.web;

import app.favorite.FavoriteServiceClient;
import app.recipe.model.Recipe;
import app.recipe.service.RecipeService;
import app.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
@RequestMapping("/favorites")
public class FavoriteController {
    private final FavoriteServiceClient favoriteServiceClient;
    private final RecipeService recipeService;

    @GetMapping
    public String getFavoriteRecipes(Model model, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        List<UUID> favoriteRecipeIds = favoriteServiceClient.getFavoriteRecipeIds(customUserDetails.getId());
        List<Recipe> favoriteRecipes = recipeService.getRecipesByIds(favoriteRecipeIds);

        model.addAttribute("favoriteRecipes", favoriteRecipes);
        return "favorite-recipes";
    }

    @PostMapping("/add")
    public String addFavorite(@RequestParam UUID recipeId,
                              @AuthenticationPrincipal CustomUserDetails customUserDetails,
                              RedirectAttributes redirectAttributes) {

        boolean success = favoriteServiceClient.addFavorite(customUserDetails.getId(), recipeId);

        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Recipe added to favorites!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add favorite.");
        }

        return "redirect:/recipes/" + recipeId;
    }

    @DeleteMapping("/remove")
    public String removeFavorite(@RequestParam UUID recipeId,
                                 @AuthenticationPrincipal CustomUserDetails customUserDetails,
                                 RedirectAttributes redirectAttributes) {

        boolean success = favoriteServiceClient.removeFavorite(customUserDetails.getId(), recipeId);

        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Recipe removed from favorites!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to remove favorite.");
        }

        return "redirect:/favorites";
    }
}