package app.web;

import app.comment.service.CommentService;
import app.like.service.LikeService;
import app.recipe.service.RecipeService;
import app.user.service.UserService;
import app.web.dto.UserWithRole;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRestController {

    private final UserService userService;
    private final RecipeService recipeService;
    private final CommentService commentService;
    private final LikeService likeService;

    @GetMapping("/total-users")
    public ResponseEntity<Long> getTotalUsers() {
        long totalUsers = userService.countUsers();
        return ResponseEntity.ok(totalUsers);
    }

    @GetMapping("/total-recipes")
    public ResponseEntity<Long> getTotalRecipes() {
        long totalRecipes = recipeService.countRecipes();
        return ResponseEntity.ok(totalRecipes);
    }

    @GetMapping("/total-comments")
    public ResponseEntity<Long> getTotalComments() {
        long totalComments = commentService.countComments();
        return ResponseEntity.ok(totalComments);
    }

    @GetMapping("/total-likes")
    public ResponseEntity<Long> getTotalLikes() {
        long totalLikes = likeService.countLikes();
        return ResponseEntity.ok(totalLikes);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserWithRole>> getUsers() {
        List<UserWithRole> all = userService.getAll();

        if (all.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(all);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<String> updateUserRole(@PathVariable UUID userId,
                                                 @RequestBody Map<String, String> requestBody) {
        String newRole = requestBody.get("role");

        if (newRole == null || newRole.isEmpty()) {
            return ResponseEntity.badRequest().body("Role cannot be empty.");
        }

        userService.updateUserRole(userId, newRole);
        return ResponseEntity.ok("User role updated successfully.");
    }
}