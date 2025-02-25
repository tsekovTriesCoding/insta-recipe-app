package app.web.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class FavoriteRecipe {

    private UUID recipeId;

    public FavoriteRecipe(UUID recipeId) {
        this.recipeId = recipeId;
    }
}
