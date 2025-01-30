package app.web;

import app.category.model.CategoryName;
import app.recipe.model.Recipe;
import app.recipe.service.RecipeService;
import app.web.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
@RequestMapping("/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    @ModelAttribute(name = "categories")
    public CategoryName[] categoryName() {
        return CategoryName.values();
    }

    @GetMapping("/all")
    public String allRecipes(Model model) {
        List<RecipeShortInfo> recipes = recipeService.getAll();

        model.addAttribute("recipes", recipes);

        return "recipes";
    }

    @GetMapping("/{id}")
    public String recipeDetails(@PathVariable UUID id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                Model model) {
        RecipeDetails recipe = recipeService.getDetailsById(id);

        boolean isCreator = recipe.getCreator().equals(userDetails.getUsername());

        model.addAttribute("recipe", recipe);
        model.addAttribute("isCreator", isCreator);

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
                            @AuthenticationPrincipal UserDetails userDetails) throws IOException {

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
        EditRecipe recipe = recipeService.getAddRecipeById(id);

        if (!model.containsAttribute("recipe")) {
            model.addAttribute("recipe", recipe);
        }

        if (!model.containsAttribute("categories")) {
            model.addAttribute("categories", CategoryName.values());
        }

        return "edit-recipe";
    }

    @PostMapping("/edit/{id}")
    public String editRecipe(@Valid EditRecipe recipe,
                             Model model,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) throws IOException {
        model.addAttribute("recipe", recipe);

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("recipe", recipe);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.recipe", bindingResult);

            return "redirect:/recipes/edit/" + recipe.getId();
        }

        recipeService.update(recipe);

        return "redirect:/recipes/" + recipe.getId();
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
}