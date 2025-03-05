package app.exception;

import java.util.UUID;

public class FavoriteNotFoundException extends RuntimeException {
    public FavoriteNotFoundException(UUID recipeId) {
        super("Favorite recipe with id " + recipeId + " not found in user's favorites list");
    }
}
