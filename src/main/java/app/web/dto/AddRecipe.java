package app.web.dto;

import app.category.model.CategoryName;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class AddRecipe {
    @Size(min = 3, max = 30)
    private String title;

    @Size(min = 10, max = 500)
    private String description;

    @NotNull
    private List<CategoryName> categories;

    @NotNull
    @Size(min = 10, max = 100)
    private String ingredients;

    @NotNull
    @Size(min = 10, max = 200)
    private String instructions;
}
