package app.web.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class RecipeShortInfo {
    private UUID id;
    private String title;
    private String description;
    private int cookTime;
    private int servings;
}
