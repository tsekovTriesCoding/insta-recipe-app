package app.web.dto;

import app.category.model.Category;
import app.comment.model.Comment;
import app.like.model.Like;
import app.user.model.User;
import lombok.Builder;
import lombok.Data;

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
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private String image;
    private Integer prepTime;
    private Integer cookTime;
    private Integer servings;
    private User createdBy;
    private List<Category> categories;
    private List<Comment> comments;
    private Integer likes;

    public String getCreator() {
        return createdBy.getUsername();
    }
}
