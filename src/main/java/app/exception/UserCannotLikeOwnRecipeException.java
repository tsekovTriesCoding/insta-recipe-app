package app.exception;

public class UserCannotLikeOwnRecipeException extends RuntimeException {
    public UserCannotLikeOwnRecipeException(String message) {
        super(message);
    }
}
