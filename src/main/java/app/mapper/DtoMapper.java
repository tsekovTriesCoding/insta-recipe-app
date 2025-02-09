package app.mapper;

import app.category.model.Category;
import app.comment.model.Comment;
import app.recipe.model.Recipe;
import app.user.model.User;
import app.web.dto.*;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DtoMapper {

    public static UserProfileInfo mapUserToUserProfileInfo(User user) {
        return UserProfileInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .profilePictureUrl(user.getProfilePicture())
                .dateRegistered(user.getDateRegistered())
                .dateUpdated(user.getDateUpdated())
                .isActive(user.getIsActive())
                .build();
    }

    public static UserWithRole mapUserToUserWithRole(User user) {
        return UserWithRole.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    public static RecipeForAdminPageInfo mapRecipeToRecipeForAdminPageInfo(Recipe recipe) {
        return RecipeForAdminPageInfo.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .author(recipe.getCreatedBy().getUsername())
                .createdDate(recipe.getCreatedDate())
                .build();
    }

    public static RecipeShortInfo mapRecipeToRecipeShortInfo(Recipe recipe) {
        return RecipeShortInfo.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .cookTime(recipe.getCookTime())
                .servings(recipe.getServings())
                .image(recipe.getImage())
                .build();
    }

    public static CommentByRecipe mapCommentToCommentByRecipe(Comment comment) {
        return CommentByRecipe.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdBy(comment.getCreator().getUsername())
                .createdDate(comment.getCreatedDate())
                .build();
    }

    public static CategoryShort mapCategoryToCategoryShort(Category category) {
        return CategoryShort.builder()
                .id(category.getId())
                .name(category.getName())
                .imageUrl(category.getImageUrl())
                .build();
    }

    public static CategoryDetails mapCategoryToCategoryDetails(Category category) {
        return CategoryDetails.builder()
                .id(category.getId())
                .name(category.getName().getValue())
                .description(category.getDescription())
                .recipes(category.getRecipes())
                .build();
    }
}
