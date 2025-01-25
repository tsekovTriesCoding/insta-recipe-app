package app.web.dto;

import app.category.model.CategoryName;
import app.vallidation.annotation.FileSize;
import app.vallidation.annotation.ImageFileType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Builder
@Data
public class EditRecipe {
    private UUID id;

    @Size(min = 3, max = 30)
    private String title;

    @Size(min = 10, max = 500)
    private String description;

    @NotEmpty
    private List<CategoryName> categories;

    @FileSize(maxSize = 3145728, message = "File size must be less than 3MB")
    @ImageFileType
    private MultipartFile image;

    @NotNull
    @Size(min = 10, max = 100)
    private String ingredients;

    @NotNull
    @Size(min = 10, max = 200)
    private String instructions;

    @NotNull
    @Min(1)
    private Integer cookTime;

    private Integer prepTime;

    @NotNull
    @Min(1)
    private Integer servings;
}
