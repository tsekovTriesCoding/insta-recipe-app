package app.web;

import app.category.model.CategoryName;
import app.recipe.model.Recipe;
import app.recipe.service.RecipeService;
import app.web.dto.AddRecipe;
import app.web.dto.RecipeDetails;
import app.web.dto.RecipeShortInfo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
@RequestMapping("/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    @GetMapping("/all")
    public String allRecipes(Model model) {
        List<RecipeShortInfo> recipes = recipeService.getAll();
        model.addAttribute("recipes", recipes);

        return "recipes";
    }

    @GetMapping("/{id}")
    public String recipeDetails(@PathVariable UUID id, Model model) {
        RecipeDetails recipe = recipeService.getById(id);
        model.addAttribute("recipe", recipe);

        return "recipe-details";
    }

    @GetMapping("/add")
    public String addRecipe(Model model) {
        if (!model.containsAttribute("recipe")) {
            model.addAttribute("recipe", new AddRecipe());
        }

        if (!model.containsAttribute("categories")) {
            model.addAttribute("categories", CategoryName.values());
        }

        return "add-recipe";
    }

    @PostMapping("/add")
    public String addRecipe(@Valid AddRecipe recipe,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes,
                            @AuthenticationPrincipal UserDetails userDetails) throws IOException {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("recipe", recipe);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.recipe", bindingResult);

            return "redirect:/recipes/add";
        }

        Recipe newRecipe = recipeService.create(recipe, userDetails.getUsername());

        return "redirect:/recipes/" + newRecipe.getId();
    }
}
