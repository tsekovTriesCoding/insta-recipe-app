package app.web;

import app.recipe.service.RecipeService;
import app.web.dto.RecipeForAdminPageInfo;
import app.web.dto.RecipeShortInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminViewController {

    private final RecipeService recipeService;

    @GetMapping
    public String adminDashboard() {
        return "admin-dashboard";
    }

    @GetMapping("/users")
    public String adminUserManagement() {
        return "admin-user-management";
    }

    @GetMapping("/recipes")
    public String adminRecipeManagement(Model model) {
        List<RecipeForAdminPageInfo> recipes = recipeService.getAllForAdmin();

        model.addAttribute("recipes", recipes);
        return "admin-recipe-management";
    }
}