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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
@RequestMapping("/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    @GetMapping("/all")
    public ModelAndView allRecipes() {
        ModelAndView mav = new ModelAndView("recipes");

        List<RecipeShortInfo> recipes = recipeService.getAll();
        mav.addObject("recipes", recipes);

        return mav;
    }

    @GetMapping("/{id}")
    public ModelAndView recipeDetails(@PathVariable UUID id) {
        ModelAndView mav = new ModelAndView("recipe-details");

        RecipeDetails recipe = recipeService.getById(id);
        mav.addObject("recipe", recipe);
        return mav;
    }

    @GetMapping("/add")
    public ModelAndView addRecipe() {
        ModelAndView mav = new ModelAndView("add-recipe");
        CategoryName[] categories = CategoryName.values();

        mav.addObject("recipe", new AddRecipe());
        mav.addObject("categories", categories);

        return mav;
    }

    @PostMapping("/add")
    public ModelAndView addRecipe(@Valid AddRecipe recipe,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes,
                                  @AuthenticationPrincipal UserDetails userDetails) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("recipe", recipe);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.recipe", bindingResult);
            return new ModelAndView("redirect:/recipes/add");
        }

        recipeService.create(recipe, userDetails.getUsername());

        return new ModelAndView("redirect:/home");
    }
}
