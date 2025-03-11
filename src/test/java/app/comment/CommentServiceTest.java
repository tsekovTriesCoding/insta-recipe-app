package app.comment;

import app.activitylog.event.ActivityLogEvent;
import app.comment.model.Comment;
import app.comment.repository.CommentRepository;
import app.comment.service.CommentService;
import app.recipe.model.Recipe;
import app.recipe.service.RecipeService;
import app.user.model.User;
import app.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private RecipeService recipeService;

    @Mock
    private UserService userService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CommentService commentService;

    private UUID recipeId;
    private UUID userId;
    private UUID commentId;
    private User user;
    private Recipe recipe;
    private Comment comment1;
    private Comment comment2;

    @BeforeEach
    void setUp() {
        recipeId = UUID.randomUUID();
        userId = UUID.randomUUID();
        commentId = UUID.randomUUID();

        user = User.builder()
                .id(userId)
                .username("JohnDoe")
                .build();

        User recipeOwner = User.builder()
                .id(UUID.randomUUID())
                .username("RecipeOwner")
                .build();

        recipe = Recipe.builder()
                .id(recipeId)
                .title("Pancakes")
                .createdBy(recipeOwner)
                .build();

        comment1 = Comment.builder()
                .id(UUID.randomUUID())
                .content("Great recipe!")
                .creator(user)
                .recipe(recipe)
                .createdDate(LocalDateTime.now().minusDays(1))
                .build();

        comment2 = Comment.builder()
                .id(UUID.randomUUID())
                .content("Delicious!")
                .creator(user)
                .recipe(recipe)
                .createdDate(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldSaveCommentWhenRecipeAndUserExist() {
        String content = "This recipe is amazing!";

        when(recipeService.getById(recipeId)).thenReturn(recipe);
        when(userService.getUserById(userId)).thenReturn(user);

        ArgumentCaptor<ActivityLogEvent> eventCaptor = ArgumentCaptor.forClass(ActivityLogEvent.class);

        commentService.add(content, recipeId, userId);

        verify(commentRepository).save(argThat(comment ->
                comment.getContent().equals(content) &&
                        comment.getRecipe().equals(recipe) &&
                        comment.getCreator().equals(user) &&
                        comment.getCreatedDate() != null
        ));

        Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(eventCaptor.capture());

        ActivityLogEvent capturedEvent = eventCaptor.getValue();
        String expectedAction = "You have successfully commented [%s], on recipe: %s".formatted(content, recipe.getTitle());

        assertEquals(userId, capturedEvent.getUserId());
        assertEquals(expectedAction, capturedEvent.getAction());
    }

    @Test
    void shouldReturnCommentsByRecipeId() {
        when(commentRepository.findAllByRecipeIdOrderByCreatedDate(recipeId))
                .thenReturn(List.of(comment1, comment2));

        List<Comment> result = commentService.getCommentsByRecipeId(recipeId);

        assertThat(result).hasSize(2)
                .extracting(Comment::getContent)
                .containsExactly("Great recipe!", "Delicious!");

        verify(commentRepository).findAllByRecipeIdOrderByCreatedDate(recipeId);
    }

    @Test
    void shouldReturnFalseWhenCommentNotFound() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        boolean result = commentService.deleteComment(commentId, "JohnDoe");

        assertThat(result).isFalse();
        verify(commentRepository, never()).delete(any());
    }

    @Test
    void shouldDeleteCommentWhenUserIsCommentCreator() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment1));

        boolean result = commentService.deleteComment(commentId, "JohnDoe");

        ArgumentCaptor<ActivityLogEvent> eventCaptor = ArgumentCaptor.forClass(ActivityLogEvent.class);

        verify(commentRepository).delete(comment1);

        Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(eventCaptor.capture());

        ActivityLogEvent capturedEvent = eventCaptor.getValue();
        String expectedAction = "You have successfully removed your comment [%s], from recipe: %s".formatted("Great recipe!", recipe.getTitle());

        assertThat(result).isTrue();
        assertEquals(userId, capturedEvent.getUserId());
        assertEquals(expectedAction, capturedEvent.getAction());
    }

    @Test
    void shouldDeleteCommentWhenUserIsRecipeCreator() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment1));

        boolean result = commentService.deleteComment(commentId, "RecipeOwner");

        assertThat(result).isTrue();
        verify(commentRepository).delete(comment1);

        Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(Mockito.any(ActivityLogEvent.class));
    }

    @Test
    void shouldReturnFalseWhenUserIsNotAuthorizedToDelete() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment1));

        boolean result = commentService.deleteComment(commentId, "RandomUser");

        assertThat(result).isFalse();
        verify(commentRepository, never()).delete(any());
    }

    @Test
    void shouldReturnAllCommentsForAdminPage() {
        when(commentRepository.findAll()).thenReturn(List.of(comment1, comment2));

        List<Comment> result = commentService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getContent()).isEqualTo(comment1.getContent());
        assertThat(result.get(1).getContent()).isEqualTo(comment2.getContent());

        verify(commentRepository).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoCommentsExist() {
        when(commentRepository.findAll()).thenReturn(List.of());

        List<Comment> result = commentService.getAll();

        assertThat(result).isEmpty();
        verify(commentRepository).findAll();
    }
}