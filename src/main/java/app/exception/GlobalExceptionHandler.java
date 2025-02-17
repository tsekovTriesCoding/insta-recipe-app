package app.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public String handleUserNotFoundException(UserNotFoundException ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        return "error-page";
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public String handleCategoryNotFoundException(CategoryNotFoundException ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        return "error-page";
    }

    @ExceptionHandler(RecipeNotFoundException.class)
    public String handleRecipeNotFoundException(RecipeNotFoundException ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        return "error-page";
    }
}
