package app.comment.service;

import app.comment.model.Comment;
import app.comment.repository.CommentRepository;
import app.recipe.model.Recipe;
import app.recipe.service.RecipeService;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.CommentDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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

    public List<CommentDTO> getCommentsByRecipeId(UUID recipeId) {
        return commentRepository.findAllByRecipeId(recipeId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private CommentDTO convertToDTO(Comment comment) {
        return new CommentDTO(comment.getId(),
                comment.getContent(),
                comment.getCreator().getUsername(),
                comment.getCreatedDate());
    }
}
