package app.web;

import app.exception.RecipeAlreadyLikedException;
import app.exception.UserCannotLikeOwnRecipeException;
import app.like.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.UUID;

@RequiredArgsConstructor
@Controller
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/like/{recipeId}")
    public String like(@PathVariable UUID recipeId, @AuthenticationPrincipal UserDetails userDetails) {
        likeService.like(userDetails.getUsername(), recipeId);

        return "redirect:/recipes/" + recipeId;
    }

    @ExceptionHandler(UserCannotLikeOwnRecipeException.class)
    public String handleCannotLikeOwnRecipe(UserCannotLikeOwnRecipeException ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        return "error-page";
    }

    @ExceptionHandler(RecipeAlreadyLikedException.class)
    public String handleRecipeAlreadyLiked(RecipeAlreadyLikedException ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        return "error-page";
    }
}
