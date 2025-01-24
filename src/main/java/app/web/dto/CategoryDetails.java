package app.web.dto;

import app.recipe.model.Recipe;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Builder
@Data
public class CategoryDetails {
    private UUID id;
    private String name;
    private String description;
    private List<Recipe> recipes;
}
