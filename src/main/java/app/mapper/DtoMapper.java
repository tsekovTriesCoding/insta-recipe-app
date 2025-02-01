package app.mapper;

import app.recipe.model.Recipe;
import app.user.model.User;
import app.web.dto.RecipeForAdminPageInfo;
import app.web.dto.RecipeShortInfo;
import app.web.dto.UserProfileInfo;
import app.web.dto.UserWithRole;
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
}
