package app.web;

import app.comment.service.CommentService;
import app.mapper.DtoMapper;
import app.recipe.service.RecipeService;
import app.user.service.UserService;
import app.web.dto.CommentForAdminPage;
import app.web.dto.RecipeForAdminPageInfo;
import app.web.dto.UserWithRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final RecipeService recipeService;
    private final CommentService commentService;

    @GetMapping("/users")
    public ModelAndView adminUserManagement(@RequestParam(value = "message", required = false) String message) {
        List<UserWithRole> users = userService.getAll()
                .stream()
                .map(DtoMapper::mapUserToUserWithRole)
                .collect(Collectors.toList());

        ModelAndView modelAndView = new ModelAndView("admin-user-management");
        modelAndView.addObject("users", users);

        if (message != null) {
            modelAndView.addObject("message", message);
        }

        return modelAndView;
    }

    @PutMapping("/users/change-role/{userId}")
    public ModelAndView changeUserRole(@PathVariable UUID userId) {
        userService.updateUserRole(userId);

        return new ModelAndView("redirect:/admin/users", "message", "You have successfully changed the user's role");
    }

    @PutMapping("/users/change-status/{userId}")
    public ModelAndView changeUserStatus(@PathVariable UUID userId) {
        userService.updateUserStatus(userId);

        return new ModelAndView("redirect:/admin/users", "message", "You have successfully changed the user's status");
    }

    @GetMapping("/recipes")
    public ModelAndView adminRecipeManagement(@RequestParam(value = "message", required = false) String message) {
        List<RecipeForAdminPageInfo> recipes = recipeService.getAllForAdmin()
                .stream()
                .map(DtoMapper::mapRecipeToRecipeForAdminPageInfo)
                .toList();

        ModelAndView modelAndView = new ModelAndView("admin-recipe-management");
        modelAndView.addObject("recipes", recipes);

        if (message != null) {
            modelAndView.addObject("message", message);
        }

        return modelAndView;
    }

    @DeleteMapping("/recipes/{recipeId}")
    public ModelAndView deleteRecipe(@PathVariable UUID recipeId) {
        recipeService.delete(recipeId);

        return new ModelAndView("redirect:/admin/recipes", "message", "You have successfully deleted recipe with [id]: " + recipeId);
    }

    @GetMapping("/comments")
    public ModelAndView adminCommentManagement(@RequestParam(value = "message", required = false) String message) {
        List<CommentForAdminPage> comments = commentService.getAll();

        ModelAndView modelAndView = new ModelAndView("admin-comment-management");
        modelAndView.addObject("comments", comments);

        if (message != null) {
            modelAndView.addObject("message", message);
        }

        return modelAndView;
    }

    @DeleteMapping("/comments/{commentId}")
    public ModelAndView deleteComment(@PathVariable UUID commentId) {
        commentService.delete(commentId);

        return new ModelAndView("redirect:/admin/comments", "message", "You have successfully deleted comment with [id]: " + commentId);
    }
}