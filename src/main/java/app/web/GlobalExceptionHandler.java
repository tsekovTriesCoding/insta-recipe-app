package app.web;

import app.exception.CategoryNotFoundException;
import app.exception.ImageUploadException;
import app.exception.RecipeNotFoundException;
import app.exception.UserNotFoundException;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            AccessDeniedException.class,
            NoResourceFoundException.class,
            MethodArgumentTypeMismatchException.class,
            MissingRequestValueException.class})
    public ModelAndView handleNotFoundExceptions() {

        return new ModelAndView("not-found");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ModelAndView handleAnyOtherException(Exception exception) {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("internal-server-error");
        modelAndView.addObject("errorMessage", exception.getClass().getSimpleName());

        return modelAndView;
    }
}