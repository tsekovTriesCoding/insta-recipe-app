package app.web;

import app.comment.service.CommentService;
import app.recipe.service.RecipeService;
import app.user.service.UserService;
import app.web.dto.CommentForAdminPage;
import app.web.dto.RecipeForAdminPageInfo;
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

    @GetMapping("/users")
    public ResponseEntity<List<UserWithRole>> getUsers() {
        List<UserWithRole> all = userService.getAll();

        if (all.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(all);
    }

    @GetMapping("/recipes")
    public ResponseEntity<List<RecipeForAdminPageInfo>> getRecipes() {
        List<RecipeForAdminPageInfo> all = recipeService.getAllForAdmin();

        if (all.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(all);
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<String> updateUserRole(@PathVariable UUID userId,
                                                 @RequestBody Map<String, String> requestBody) {
        String newRole = requestBody.get("role");

        if (newRole == null || newRole.isEmpty()) {
            return ResponseEntity.badRequest().body("Role cannot be empty.");
        }

        boolean updated = userService.updateUserRole(userId, newRole);

        if (updated) {
            return ResponseEntity.ok("User role updated successfully");
        }

        return ResponseEntity.badRequest().body("Failed to update user role");
    }

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<String> updateUserStatus(@PathVariable UUID userId,
                                                   @RequestBody Map<String, String> requestBody) {

        String newStatus = requestBody.get("isActive");

        if (newStatus == null || newStatus.isEmpty()) {
            return ResponseEntity.badRequest().body("Status cannot be empty.");
        }

        boolean updated = userService.updateUserStatus(userId, Boolean.parseBoolean(newStatus));

        if (updated) {
            return ResponseEntity.ok("User status updated successfully");
        }

        return ResponseEntity.badRequest().body("Failed to update user status");

    }

    @DeleteMapping("/recipes/{recipeId}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable UUID recipeId) {
        recipeService.delete(recipeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/comments")
    public ResponseEntity<List<CommentForAdminPage>> getComments() {
        List<CommentForAdminPage> comments = commentService.getAll();

        if (comments.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(comments);
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID commentId) {
        commentService.delete(commentId);
        return ResponseEntity.noContent().build();
    }
}