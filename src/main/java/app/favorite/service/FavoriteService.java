package app.favorite.service;

import app.activitylog.event.ActivityLogEvent;
import app.exception.AlreadyFavoritedException;
import app.exception.FavoriteNotFoundException;
import app.favorite.model.Favorite;
import app.favorite.repository.FavoriteRepository;
import app.recipe.model.Recipe;
import app.recipe.service.RecipeService;
import app.user.model.User;
import app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserService userService;
    private final RecipeService recipeService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void addRecipeToFavorites(UUID userId, UUID recipeId) {
        User user = userService.getUserById(userId);
        Recipe recipe = recipeService.getById(recipeId);

        if (favoriteRepository.findByUserIdAndRecipeId(userId, recipeId).isPresent()) {
            throw new AlreadyFavoritedException(userId, recipeId);
        }

        Favorite favorite = Favorite.builder()
                .user(user)
                .recipe(recipe)
                .build();

        favoriteRepository.save(favorite);

        eventPublisher.publishEvent(new ActivityLogEvent(user.getId(), "You have successfully added recipe: " + recipe.getTitle() + " to your favorites"));
    }

    @Transactional
    public void removeRecipeFromFavorites(UUID userId, UUID recipeId) {
        Favorite favorite = favoriteRepository.findByUserIdAndRecipeId(userId, recipeId)
                .orElseThrow(() -> new FavoriteNotFoundException(recipeId));

        favoriteRepository.delete(favorite);

        Recipe recipe = recipeService.getById(favorite.getRecipe().getId());

        eventPublisher.publishEvent(new ActivityLogEvent(recipe.getCreatedBy().getId(),
                "You have successfully removed recipe: " + recipe.getTitle() + " from your favorites"));
    }

    public List<Recipe> getUserFavoriteRecipes(UUID userId) {
        List<UUID> favoriteRecipesIds = favoriteRepository.findAllByUser_Id(userId)
                .stream()
                .map(favorite -> favorite.getRecipe().getId())
                .toList();

        return recipeService.getRecipesByIds(favoriteRecipesIds);
    }

    public boolean isFavorite(UUID id, UUID recipeId) {
        return favoriteRepository.findByUserIdAndRecipeId(id, recipeId).isPresent();
    }
}