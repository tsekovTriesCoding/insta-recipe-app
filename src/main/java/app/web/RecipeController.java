package app.web;

import app.recipe.model.Recipe;
import app.recipe.service.RecipeService;
import app.web.dto.RecipeDetails;
import app.web.dto.RecipeShortInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

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
}
