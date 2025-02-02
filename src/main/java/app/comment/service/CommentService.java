package app.comment.service;

import app.comment.model.Comment;
import app.comment.repository.CommentRepository;
import app.mapper.DtoMapper;
import app.recipe.model.Recipe;
import app.recipe.service.RecipeService;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.CommentByRecipe;
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

    @Transactional
    public void add(String content, UUID id, String username) {
        Recipe recipe = recipeService.getById(id);
        User user = userService.getByUsername(username);

        Comment comment = Comment.builder()
                .content(content)
                .recipe(recipe)
                .creator(user)
                .createdDate(LocalDateTime.now())
                .build();

        commentRepository.save(comment);
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
        if (comment.getCreator().getUsername().equals(username) ||
                recipe.getCreatedBy().getUsername().equals(username)) {

            commentRepository.delete(comment);
            return true;
        }

        return false;
    }
}