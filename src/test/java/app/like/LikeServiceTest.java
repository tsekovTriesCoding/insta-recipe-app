package app.like;

import app.activitylog.event.ActivityLogEvent;
import app.exception.RecipeAlreadyLikedException;
import app.exception.UserCannotLikeOwnRecipeException;
import app.like.model.Like;
import app.like.repository.LikeRepository;
import app.like.service.LikeService;
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
import org.springframework.dao.DataIntegrityViolationException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private UserService userService;

    @Mock
    private RecipeService recipeService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private LikeService likeService;

    private UUID userId;
    private UUID recipeId;
    private User user;
    private Recipe recipe;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        recipeId = UUID.randomUUID();

        user = new User();
        user.setId(userId);

        recipe = new Recipe();
        recipe.setId(recipeId);
        User recipeCreator = new User();
        recipeCreator.setId(UUID.randomUUID()); // Different user
        recipe.setCreatedBy(recipeCreator);
    }

    @Test
    void shouldLikeRecipeSuccessfully() {
        when(userService.getUserById(userId)).thenReturn(user);
        when(recipeService.getById(recipeId)).thenReturn(recipe);
        when(likeRepository.save(any(Like.class))).thenReturn(new Like());

        ArgumentCaptor<ActivityLogEvent> eventCaptor = ArgumentCaptor.forClass(ActivityLogEvent.class);

        likeService.like(userId, recipeId);

        verify(userService).getUserById(userId);
        verify(recipeService).getById(recipeId);
        verify(likeRepository).save(any(Like.class));

        Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(eventCaptor.capture());

        ActivityLogEvent capturedEvent = eventCaptor.getValue();
        String expectedAction = "You have successfully liked recipe: %s".formatted(recipe.getTitle());

        assertEquals(userId, capturedEvent.getUserId());
        assertEquals(expectedAction, capturedEvent.getAction());
    }

    @Test
    void shouldThrowExceptionWhenUserLikesOwnRecipe() {
        recipe.setCreatedBy(user);

        when(userService.getUserById(userId)).thenReturn(user);
        when(recipeService.getById(recipeId)).thenReturn(recipe);

        assertThrows(UserCannotLikeOwnRecipeException.class, () -> likeService.like(userId, recipeId));

        verify(likeRepository, never()).save(any(Like.class));
    }

    @Test
    void shouldThrowExceptionWhenRecipeAlreadyLiked() {
        when(userService.getUserById(userId)).thenReturn(user);
        when(recipeService.getById(recipeId)).thenReturn(recipe);
        doThrow(new DataIntegrityViolationException("Unique constraint violated")).when(likeRepository).save(any(Like.class));

        assertThrows(RecipeAlreadyLikedException.class, () -> likeService.like(userId, recipeId));
    }

    @Test
    void shouldReturnTrueWhenUserHasLikedRecipe() {
        when(likeRepository.existsByUser_IdAndRecipe_Id(userId, recipeId)).thenReturn(true);

        boolean result = likeService.userHasLikedRecipe(userId, recipeId);

        assertTrue(result);
        verify(likeRepository).existsByUser_IdAndRecipe_Id(userId, recipeId);
    }

    @Test
    void shouldReturnFalseWhenUserHasNotLikedRecipe() {
        when(likeRepository.existsByUser_IdAndRecipe_Id(userId, recipeId)).thenReturn(false);

        boolean result = likeService.userHasLikedRecipe(userId, recipeId);

        assertFalse(result);
        verify(likeRepository).existsByUser_IdAndRecipe_Id(userId, recipeId);
    }
}