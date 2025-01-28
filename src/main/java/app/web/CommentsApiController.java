package app.web;

import app.comment.service.CommentService;
import app.web.dto.CommentDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
