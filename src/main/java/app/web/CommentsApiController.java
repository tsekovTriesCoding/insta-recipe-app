package app.web;

import app.comment.service.CommentService;
import app.mapper.DtoMapper;
import app.security.CustomUserDetails;
import app.web.dto.CommentByRecipe;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/comments")
public class CommentsApiController {

    private final CommentService commentService;

    @GetMapping("/{recipeId}")
    public ResponseEntity<List<CommentByRecipe>> getCommentsByRecipe(@PathVariable UUID recipeId) {
        List<CommentByRecipe> comments = commentService.getCommentsByRecipeId(recipeId)
                .stream()
                .map(DtoMapper::mapCommentToCommentByRecipe)
                .toList();

        // If no comments found, return 204 No Content
        if (comments.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        // Return comments with 200 OK
        return ResponseEntity.ok(comments);
    }

    @DeleteMapping("/delete/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID commentId,
                                              @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        boolean isDeleted = commentService.deleteComment(commentId, customUserDetails.getUsername());

        if (isDeleted) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}