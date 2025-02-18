package app.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminViewController {

    @GetMapping("/users")
    public String adminUserManagement() {
        return "admin-user-management";
    }

    @GetMapping("/recipes")
    public String adminRecipeManagement() {
        return "admin-recipe-management";
    }

    @GetMapping("/comments")
    public String adminCommentManagement() {
        return "admin-comment-management";
    }
}