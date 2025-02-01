package app.exception;

public class RecipeAlreadyLikedException extends RuntimeException {
    public RecipeAlreadyLikedException(String message) {
        super(message);
    }
}
