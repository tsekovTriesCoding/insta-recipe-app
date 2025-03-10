package app.exception;

import feign.FeignException;
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

    @ExceptionHandler(ImageUploadException.class)
    public String handleImageUploadException(ImageUploadException ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        return "error-page";
    }

    @ExceptionHandler(FeignException.class)
    public String handleFeignException(Model model) {
        model.addAttribute("error", "The activity log service is currently unavailable.");
        return "error-page";
    }
}