package app.comment;

import app.comment.model.Comment;
import app.comment.repository.CommentRepository;
import app.comment.service.CommentService;
import app.recipe.model.Recipe;
import app.recipe.service.RecipeService;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.CommentByRecipe;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private RecipeService recipeService;

    @Mock
    private UserService userService;

    @InjectMocks
    private CommentService commentService;

    @Test
    void addShouldSaveCommentWhenRecipeAndUserExist() {
        // Arrange
        UUID recipeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String content = "This recipe is amazing!";

        Recipe recipe = Recipe.builder()
                .id(recipeId)
                .title("Pancakes")
                .build();

        User user = User.builder()
                .id(userId)
                .username("JohnDoe")
                .build();

        when(recipeService.getById(recipeId)).thenReturn(recipe);
        when(userService.getUserById(userId)).thenReturn(user);

        // Act
        commentService.add(content, recipeId, userId);

        // Assert
        verify(commentRepository).save(argThat(comment ->
                comment.getContent().equals(content) &&
                        comment.getRecipe().equals(recipe) &&
                        comment.getCreator().equals(user) &&
                        comment.getCreatedDate() != null
        ));
    }

    @Test
    void getCommentsByRecipeIdShouldReturnComments() {
        // Arrange: Create test data
        UUID recipeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User user = User.builder()
                .id(userId)
                .username("JohnDoe")
                .build();

        Recipe pancakes = Recipe.builder()
                .id(recipeId)
                .title("Pancakes")
                .build();

        Comment comment1 = Comment.builder()
                .id(UUID.randomUUID())
                .content("Great recipe!")
                .recipe(pancakes)
                .creator(user)
                .createdDate(LocalDateTime.now().minusDays(1))
                .build();

        Comment comment2 = Comment.builder()
                .id(UUID.randomUUID())
                .content("Delicious!")
                .recipe(pancakes)
                .creator(user)
                .createdDate(LocalDateTime.now())
                .build();

        List<Comment> mockComments = List.of(comment1, comment2);

        when(commentRepository.findAllByRecipeIdOrderByCreatedDate(recipeId)).thenReturn(mockComments);

        // Act: Call the method
        List<CommentByRecipe> result = commentService.getCommentsByRecipeId(recipeId);

        // Assert: Verify correct mapping
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getContent()).isEqualTo("Great recipe!");
        assertThat(result.get(1).getContent()).isEqualTo("Delicious!");

        // Verify that repository method was called once
        verify(commentRepository, times(1)).findAllByRecipeIdOrderByCreatedDate(recipeId);
    }

    @Test
    void deleteCommentShouldReturnFalseWhenCommentNotFound() {
        UUID commentId = UUID.randomUUID();
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        boolean result = commentService.deleteComment(commentId, "JohnDoe");

        assertThat(result).isFalse();
        verify(commentRepository, never()).delete(any());
    }

    @Test
    void deleteCommentShouldDeleteWhenUserIsCommentCreator() {
        UUID commentId = UUID.randomUUID();

        User creator = User.builder()
                .id(UUID.randomUUID())
                .username("JohnDoe")
                .build();

        User recipeOwner = User.builder()
                .id(UUID.randomUUID())
                .username("RecipeOwner")
                .build();

        Recipe recipe = Recipe.builder()
                .id(UUID.randomUUID())
                .title("Pancakes")
                .createdBy(recipeOwner)
                .build();

        Comment comment = Comment.builder()
                .id(commentId)
                .content("Nice recipe!")
                .creator(creator)
                .recipe(recipe)
                .build();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        boolean result = commentService.deleteComment(commentId, "JohnDoe");

        assertThat(result).isTrue();
        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    void deleteCommentShouldDeleteWhenUserIsRecipeCreator() {
        UUID commentId = UUID.randomUUID();
        User creator = User.builder()
                .id(UUID.randomUUID())
                .username("JohnDoe")
                .build();

        User recipeOwner = User.builder()
                .id(UUID.randomUUID())
                .username("RecipeOwner")
                .build();

        Recipe recipe = Recipe.builder()
                .id(UUID.randomUUID())
                .title("Pancakes")
                .createdBy(recipeOwner)
                .build();

        Comment comment = Comment.builder()
                .id(commentId)
                .content("Looks tasty!")
                .creator(creator)
                .recipe(recipe)
                .build();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        boolean result = commentService.deleteComment(commentId, "RecipeOwner");

        assertThat(result).isTrue();
        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    void deleteCommentShouldReturnFalseWhenUserIsNotAuthorized() {
        UUID commentId = UUID.randomUUID();
        User creator = User.builder()
                .id(UUID.randomUUID())
                .username("JohnDoe")
                .build();

        User recipeOwner = User.builder()
                .id(UUID.randomUUID())
                .username("RecipeOwner")
                .build();

        Recipe recipe = Recipe.builder()
                .id(UUID.randomUUID())
                .title("Pancakes")
                .createdBy(recipeOwner)
                .build();
        Comment comment = Comment.builder()
                .id(commentId)
                .content("Tasty!")
                .creator(creator)
                .recipe(recipe)
                .build();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        boolean result = commentService.deleteComment(commentId, "RandomUser");

        assertThat(result).isFalse();
        verify(commentRepository, never()).delete(any());
    }
}