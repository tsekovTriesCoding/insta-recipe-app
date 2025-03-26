package app.web.dto;

import app.category.model.CategoryName;
import app.vallidation.annotation.FileSize;
import app.vallidation.annotation.ImageFileType;
import app.vallidation.annotation.NotEmptyFile;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class AddRecipe {
    @Size(min = 3, max = 30, message = "Title must be between 3 and 30 symbols")
    private String title;

    @Size(min = 10, max = 1500,message = "Description must be between 10 and 1500 symbols")
    private String description;

    @NotEmpty(message = "Enter at least one category")
    private List<CategoryName> categories;

    @FileSize(maxSize = 3145728, message = "File size must be less than 3MB")
    @ImageFileType
    @NotEmptyFile
    private MultipartFile image;

    @NotNull
    @Size(min = 10, max = 1500, message = "Ingredients have to be between 10 and 1500 symbols")
    private String ingredients;

    @NotNull
    @Size(min = 10, max = 1500, message = "Instructions have to be between 10 and 1500 symbols")
    private String instructions;

    @NotNull(message = "Cook time is missing")
    @Min(1)
    private Integer cookTime;

    private Integer prepTime;

    @NotNull(message = "Servings count is missing")
    @Min(1)
    private Integer servings;
}
