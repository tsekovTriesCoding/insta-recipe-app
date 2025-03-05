package app.exception;

import java.util.UUID;

public class AlreadyFavoritedException extends RuntimeException {
    public AlreadyFavoritedException(UUID userId, UUID recipeId) {
        super("Recipe with id " + recipeId + " is already in the favorites list of user with id: " + userId);
    }
}