package app.web.dto;

import app.category.model.Category;
import app.comment.model.Comment;
import app.like.model.Like;
import app.user.model.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
@Data
public class RecipeDetails {
    private UUID id;
    private String title;
    private String description;
    private List<String> ingredients;
    private String instructions;
    private LocalDate createdDate;
    private LocalDateTime updatedDate;
    private String image;
    private int prepTime;
    private int cookTime;
    private int servings;
    private User createdBy;
    private List<Category> categories;
    private List<Comment> comments;
    private List<Like> likes;
}
