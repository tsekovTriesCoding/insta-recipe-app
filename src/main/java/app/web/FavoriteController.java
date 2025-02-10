package app.web;

import app.favorite.FavoriteServiceClient;
import app.recipe.model.Recipe;
import app.recipe.service.RecipeService;
import app.user.model.User;
import app.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/favorites")
public class FavoriteController {
    private final FavoriteServiceClient favoriteServiceClient;
    private final UserService userService;
    private final RecipeService recipeService;


    public FavoriteController(FavoriteServiceClient favoriteServiceClient,
                              UserService userService,
                              RecipeService recipeService) {
        this.favoriteServiceClient = favoriteServiceClient;
        this.userService = userService;
        this.recipeService = recipeService;
    }

    @GetMapping
    public String getFavoriteRecipes(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getByUsername(userDetails.getUsername());
        List<UUID> favoriteRecipeIds = favoriteServiceClient.getFavoriteRecipeIds(user.getId());
        List<Recipe> favoriteRecipes = recipeService.getRecipesByIds(favoriteRecipeIds);

        model.addAttribute("favoriteRecipes", favoriteRecipes);
        return "favorite-recipes";
    }

    @PostMapping("/add")
    public String addFavorite(@RequestParam UUID recipeId,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        User user = userService.getByUsername(userDetails.getUsername());
        boolean success = favoriteServiceClient.addFavorite(user.getId(), recipeId);

        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Recipe added to favorites!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add favorite.");
        }

        return "redirect:/recipes/" + recipeId;
    }

    @PostMapping("/remove")
    public String removeFavorite(@RequestParam UUID recipeId,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        User user = userService.getByUsername(userDetails.getUsername());
        boolean success = favoriteServiceClient.removeFavorite(user.getId(), recipeId);

        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Recipe removed from favorites!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to remove favorite.");
        }

        return "redirect:/recipes/" + recipeId;
    }
}