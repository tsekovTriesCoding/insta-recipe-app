package app.web;

import app.exception.AlreadyFavoritedException;
import app.exception.FavoriteNotFoundException;
import app.favorite.service.FavoriteService;
import app.recipe.model.Recipe;
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

    private final FavoriteService favoriteService;

    @GetMapping
    public String getFavoriteRecipes(Model model, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        List<Recipe> userFavoriteRecipes = favoriteService.getUserFavoriteRecipes(customUserDetails.getId());

        model.addAttribute("favoriteRecipes", userFavoriteRecipes);
        return "favorite-recipes";
    }

    @PostMapping("/add")
    public String addFavorite(@RequestParam UUID recipeId,
                              @AuthenticationPrincipal CustomUserDetails customUserDetails,
                              RedirectAttributes redirectAttributes) {

        favoriteService.addRecipeToFavorites(customUserDetails.getId(), recipeId);

        redirectAttributes.addFlashAttribute("successMessage", "Recipe added to favorites!");

        return "redirect:/recipes/" + recipeId;
    }

    @DeleteMapping("/remove")
    public String removeFavorite(@RequestParam UUID recipeId,
                                 @AuthenticationPrincipal CustomUserDetails customUserDetails,
                                 RedirectAttributes redirectAttributes) {

        favoriteService.removeRecipeFromFavorites(customUserDetails.getId(), recipeId);

        redirectAttributes.addFlashAttribute("successMessage", "Recipe removed from favorites!");

        return "redirect:/favorites";
    }

    @ExceptionHandler(AlreadyFavoritedException.class)
    public String handleAlreadyFavoritedException(AlreadyFavoritedException ex, Model model) {
        model.addAttribute("error", ex.getMessage());

        return "error-page";
    }

    @ExceptionHandler(FavoriteNotFoundException.class)
    public String handleFavoriteNotFoundException(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", "Failed to remove recipe from favorites list");

        return "redirect:/favorites";
    }
}