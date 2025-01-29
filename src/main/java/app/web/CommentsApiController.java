package app.web;

import app.comment.service.CommentService;
import app.web.dto.CommentDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/comments")
public class CommentsApiController {

    private final CommentService commentService;

    @GetMapping("/{recipeId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByRecipe(@PathVariable UUID recipeId) {
        List<CommentDTO> comments = commentService.getCommentsByRecipeId(recipeId);

        // If no comments found, return 204 No Content
        if (comments.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        // Return comments with 200 OK
        return ResponseEntity.ok(comments);
    }

    @DeleteMapping("/delete/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID commentId,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        boolean isDeleted = commentService.deleteComment(commentId, userDetails.getUsername());

        if (isDeleted) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
