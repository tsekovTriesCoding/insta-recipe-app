package app.like.service;

import app.activitylog.event.ActivityLogEvent;
import app.exception.RecipeAlreadyLikedException;
import app.exception.UserCannotLikeOwnRecipeException;
import app.like.model.Like;
import app.like.repository.LikeRepository;
import app.recipe.model.Recipe;
import app.recipe.service.RecipeService;
import app.user.model.User;
import app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final UserService userService;
    private final RecipeService recipeService;
    private final ApplicationEventPublisher eventPublisher;

    public void like(UUID userId, UUID recipeId) {
        User user = userService.getUserById(userId);
        Recipe recipe = recipeService.getById(recipeId);

        if (recipe.getCreatedBy().getId().equals(user.getId())) {
            throw new UserCannotLikeOwnRecipeException("You cannot like your own recipe.");
        }

        try {
            Like like = new Like();
            like.setUser(user);
            like.setRecipe(recipe);
            like.setLikedDate(LocalDateTime.now());

            likeRepository.save(like);

            eventPublisher.publishEvent(new ActivityLogEvent(user.getId(), "You have successfully liked recipe: " + recipe.getTitle()));
        } catch (DataIntegrityViolationException e) {
            throw new RecipeAlreadyLikedException("You have already liked this recipe");
        }
    }

    public boolean userHasLikedRecipe(UUID userId, UUID recipeId) {
        return likeRepository.existsByUser_IdAndRecipe_Id(userId, recipeId);
    }
}