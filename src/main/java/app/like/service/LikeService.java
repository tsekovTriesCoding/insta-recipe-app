package app.like.service;

import app.exception.RecipeAlreadyLikedException;
import app.like.model.Like;
import app.like.repository.LikeRepository;
import app.recipe.model.Recipe;
import app.recipe.service.RecipeService;
import app.user.model.User;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final UserService userService;
    private final RecipeService recipeService;

    public void like(String username, UUID recipeId) {
        User user = userService.getByUsername(username);
        Recipe recipe = recipeService.getById(recipeId);

        boolean userAlreadyLiked = likeRepository.existsByUser_IdAndRecipe_Id(user.getId(), recipeId);

        if (userAlreadyLiked) {
            throw new RecipeAlreadyLikedException("You have already liked this recipe");
        }

//        if (recipe.getCreatedBy().getId().equals(user.getId())) {
//            return;
//        }

        Like like = new Like();
        like.setUser(user);
        like.setRecipe(recipe);

        likeRepository.save(like);
    }
}
