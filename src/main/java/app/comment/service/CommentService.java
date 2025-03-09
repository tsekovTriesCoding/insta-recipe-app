package app.comment.service;

import app.activitylog.annotation.LogActivity;
import app.comment.model.Comment;
import app.comment.repository.CommentRepository;
import app.recipe.model.Recipe;
import app.recipe.service.RecipeService;
import app.user.model.User;
import app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final RecipeService recipeService;
    private final UserService userService;

    @LogActivity(activity = "'Added comment with content: ' + #result.content")
    @Transactional
    public Comment add(String content, UUID recipeId, UUID userId) {
        Recipe recipe = recipeService.getById(recipeId);
        User user = userService.getUserById(userId);

        Comment comment = Comment.builder()
                .content(content.trim())
                .recipe(recipe)
                .creator(user)
                .createdDate(LocalDateTime.now())
                .build();

       return commentRepository.save(comment);
    }

    public List<Comment> getCommentsByRecipeId(UUID recipeId) {
        return commentRepository.findAllByRecipeIdOrderByCreatedDate(recipeId);
    }

    @LogActivity(activity = "'You have successfully removed comment with id: ' + #commentId")
    public boolean deleteComment(UUID commentId, String username) {
        Optional<Comment> commentOpt = commentRepository.findById(commentId);

        if (commentOpt.isEmpty()) {
            return false;
        }

        Comment comment = commentOpt.get();
        Recipe recipe = comment.getRecipe();

        // Check if the user is the comment creator or the recipe creator
        if (comment.getCreator().getUsername().equals(username)) {
            commentRepository.delete(comment);
            return true;
        } else if (recipe.getCreatedBy().getUsername().equals(username)) {
            commentRepository.delete(comment);
            return true;
        }

        return false;
    }

    public List<Comment> getAll() {
        return commentRepository.findAll();
    }

    public void delete(UUID commentId) {
        commentRepository.deleteById(commentId);
    }
}