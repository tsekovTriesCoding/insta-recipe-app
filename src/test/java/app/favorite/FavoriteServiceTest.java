package app.favorite;

import app.activitylog.event.ActivityLogEvent;
import app.exception.AlreadyFavoritedException;
import app.exception.FavoriteNotFoundException;
import app.favorite.model.Favorite;
import app.favorite.repository.FavoriteRepository;
import app.favorite.service.FavoriteService;
import app.recipe.model.Recipe;
import app.recipe.service.RecipeService;
import app.user.model.User;
import app.user.service.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private UserService userService;

    @Mock
    private RecipeService recipeService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private FavoriteService favoriteService;

    private static User user;

    private static Recipe recipe;

    private static UUID userId;

    private static UUID recipeId;

    @BeforeAll
    public static void setTestData() {
        user = User.builder()
                .id(UUID.randomUUID())
                .username("user")
                .email("user@email.com")
                .password("password")
                .build();

        recipe = Recipe.builder()
                .id(UUID.randomUUID())
                .title("recipe")
                .description("description")
                .build();

        userId = user.getId();
        recipeId = recipe.getId();
    }

    @Test
    void testAddRecipeToFavorites_ShouldAddFavorite() {
        when(userService.getUserById(userId)).thenReturn(user);
        when(recipeService.getById(recipeId)).thenReturn(recipe);
        when(favoriteRepository.findByUserIdAndRecipeId(userId, recipeId)).thenReturn(Optional.empty());

        favoriteService.addRecipeToFavorites(userId, recipeId);

        ArgumentCaptor<ActivityLogEvent> eventCaptor = ArgumentCaptor.forClass(ActivityLogEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        ActivityLogEvent capturedEvent = eventCaptor.getValue();

        assertNotNull(capturedEvent);
        assertEquals("You have successfully added recipe: " + recipe.getTitle() + " to your favorites", capturedEvent.getAction());

        verify(favoriteRepository, times(1)).save(any(Favorite.class));
    }

    @Test
    public void testAddRecipeToFavorites_AlreadyFavorited_ShouldThrow() {
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();

        when(favoriteRepository.findByUserIdAndRecipeId(userId, recipeId)).thenReturn(Optional.of(new Favorite()));

        assertThrows(AlreadyFavoritedException.class, () -> favoriteService.addRecipeToFavorites(userId, recipeId));

        verify(favoriteRepository, times(0)).save(any(Favorite.class));
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void testRemoveRecipeFromFavorites_ShouldRemoveFavorite() {
        Favorite favorite = Favorite.builder()
                .user(user)
                .recipe(recipe)
                .build();

        when(favoriteRepository.findByUserIdAndRecipeId(userId, recipeId)).thenReturn(Optional.of(favorite));
        when(recipeService.getById(recipeId)).thenReturn(recipe);

        favoriteService.removeRecipeFromFavorites(userId, recipeId);

        verify(favoriteRepository, times(1)).delete(favorite);

        ArgumentCaptor<ActivityLogEvent> eventCaptor = ArgumentCaptor.forClass(ActivityLogEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        ActivityLogEvent capturedEvent = eventCaptor.getValue();
        assertNotNull(capturedEvent);
        assertEquals("You have successfully removed recipe: " + recipe.getTitle() + " from your favorites", capturedEvent.getAction());
    }

    @Test
    void testRemoveRecipeFromFavorites_FavoriteNotFound_ShouldThrow() {
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();

        when(favoriteRepository.findByUserIdAndRecipeId(userId, recipeId)).thenReturn(Optional.empty());

        assertThrows(FavoriteNotFoundException.class, () -> favoriteService.removeRecipeFromFavorites(userId, recipeId));
        verify(favoriteRepository, times(0)).delete(any(Favorite.class));
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void testGetUserFavoriteRecipes_ShouldReturnFavorites() {
        UUID userId = user.getId();

        UUID recipeId1 = UUID.randomUUID();
        UUID recipeId2 = UUID.randomUUID();

        Favorite favorite1 = Favorite.builder()
                .user(user)
                .recipe(Recipe.builder()
                        .id(recipeId1)
                        .title("Recipe 1")
                        .build())
                .build();

        Favorite favorite2 = Favorite.builder()
                .user(user)
                .recipe(Recipe.builder()
                        .id(recipeId2)
                        .title("Recipe 2")
                        .build())
                .build();

        List<Favorite> favorites = List.of(favorite1, favorite2);
        when(favoriteRepository.findAllByUser_Id(userId)).thenReturn(favorites);

        List<Recipe> expectedRecipes = List.of(
                Recipe.builder()
                        .id(recipeId1)
                        .title("Recipe 1")
                        .build(),
                Recipe.builder()
                        .id(recipeId2)
                        .title("Recipe 2")
                        .build());

        when(recipeService.getRecipesByIds(List.of(recipeId1, recipeId2))).thenReturn(expectedRecipes);

        List<Recipe> result = favoriteService.getUserFavoriteRecipes(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Recipe 1", result.get(0).getTitle());
        assertEquals("Recipe 2", result.get(1).getTitle());
    }

    @Test
    void testIsFavorite_ShouldReturnTrue() {
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();

        when(favoriteRepository.findByUserIdAndRecipeId(userId, recipeId)).thenReturn(Optional.of(new Favorite()));

        assertTrue(favoriteService.isFavorite(userId, recipeId));
    }

    @Test
    void testIsFavorite_ShouldReturnFalse() {
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();

        when(favoriteRepository.findByUserIdAndRecipeId(userId, recipeId)).thenReturn(Optional.empty());

        assertFalse(favoriteService.isFavorite(userId, recipeId));
    }
}
