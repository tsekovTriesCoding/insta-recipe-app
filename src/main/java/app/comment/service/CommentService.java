package app.comment.service;

import app.activity.ActivityLogService;
import app.comment.model.Comment;
import app.comment.repository.CommentRepository;
import app.mapper.DtoMapper;
import app.recipe.model.Recipe;
import app.recipe.service.RecipeService;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.CommentByRecipe;
import app.web.dto.CommentForAdminPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final RecipeService recipeService;
    private final UserService userService;
    private final ActivityLogService activityLogService;

    @Transactional
    public void add(String content, UUID recipeId,UUID userId) {
        Recipe recipe = recipeService.getById(recipeId);
        User user = userService.getUserById(userId);

        Comment comment = Comment.builder()
                .content(content)
                .recipe(recipe)
                .creator(user)
                .createdDate(LocalDateTime.now())
                .build();

        commentRepository.save(comment);

        activityLogService.logActivity("You have successfully added comment for recipe %s - with content: [%s]"
                .formatted(recipe.getTitle(), comment.getContent()), user.getId());
    }

    public List<CommentByRecipe> getCommentsByRecipeId(UUID recipeId) {
        return commentRepository.findAllByRecipeIdOrderByCreatedDate(recipeId)
                .stream()
                .map(DtoMapper::mapCommentToCommentByRecipe)
                .collect(Collectors.toList());
    }

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

            activityLogService.logActivity("You have successfully deleted comment for recipe %s - with content: [%s]"
                    .formatted(recipe.getTitle(), comment.getContent()), comment.getCreator().getId());

            return true;
        } else if (recipe.getCreatedBy().getUsername().equals(username)) {
            commentRepository.delete(comment);

            activityLogService.logActivity("You have successfully deleted comment for recipe %s - with content: [%s]"
                    .formatted(recipe.getTitle(), comment.getContent()), recipe.getCreatedBy().getId());

            return true;
        }

        return false;
    }

    public List<CommentForAdminPage> getAll() {
        return commentRepository.findAll()
                .stream()
                .map(DtoMapper::mapCommentToCommentForAdminPage)
                .collect(Collectors.toList());
    }

    public void delete(UUID commentId) {
        commentRepository.deleteById(commentId);
    }
}