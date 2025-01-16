package app.web;

import app.recipe.model.Recipe;
import app.recipe.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    @GetMapping("/all")
    public ModelAndView allRecipes() {
        ModelAndView mav = new ModelAndView("recipes");

        List<Recipe> recipes = recipeService.getAll();
        mav.addObject("recipes", recipes);

        return mav;
    }
}
