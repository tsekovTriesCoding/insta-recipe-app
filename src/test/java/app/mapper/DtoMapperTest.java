package app.mapper;

import app.category.model.Category;
import app.category.model.CategoryName;
import app.comment.model.Comment;
import app.recipe.model.Recipe;
import app.user.model.Role;
import app.user.model.User;
import app.web.dto.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DtoMapperTest {

    @Test
    void mapUserToUserProfileInfo_ShouldMapCorrectly() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("testUser")
                .email("test@example.com")
                .profilePicture("profile.jpg")
                .dateRegistered(LocalDateTime.now())
                .dateUpdated(LocalDateTime.now())
                .isActive(true)
                .build();

        UserProfileInfo profileInfo = DtoMapper.mapUserToUserProfileInfo(user);

        assertNotNull(profileInfo);
        assertEquals(user.getId(), profileInfo.getId());
        assertEquals(user.getUsername(), profileInfo.getUsername());
        assertEquals(user.getEmail(), profileInfo.getEmail());
        assertEquals(user.getProfilePicture(), profileInfo.getProfilePictureUrl());
        assertEquals(user.getDateRegistered(), profileInfo.getDateRegistered());
        assertEquals(user.getDateUpdated(), profileInfo.getDateUpdated());
        assertEquals(user.getIsActive(), profileInfo.getIsActive());
    }

    @Test
    void mapUserToUserWithRole_ShouldMapCorrectly() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("testUser")
                .email("test@example.com")
                .profilePicture("profile.jpg")
                .dateRegistered(LocalDateTime.now())
                .dateUpdated(LocalDateTime.now())
                .isActive(true)
                .role(Role.USER)
                .build();

        UserWithRole userWithRole = DtoMapper.mapUserToUserWithRole(user);

        assertNotNull(userWithRole);
        assertEquals(user.getId(), userWithRole.getId());
        assertEquals(user.getUsername(), userWithRole.getUsername());
        assertEquals(user.getEmail(), userWithRole.getEmail());
        assertEquals(user.getRole(), userWithRole.getRole());
    }

    @Test
    void mapRecipeToRecipeShortInfo_ShouldMapCorrectly() {
        Recipe recipe = Recipe.builder()
                .id(UUID.randomUUID())
                .title("title")
                .description("description")
                .image("image.jpg")
                .cookTime(30)
                .prepTime(4)
                .servings(2)
                .build();

        RecipeShortInfo shortInfo = DtoMapper.mapRecipeToRecipeShortInfo(recipe);

        assertNotNull(shortInfo);
        assertEquals(recipe.getId(), shortInfo.getId());
        assertEquals(recipe.getTitle(), shortInfo.getTitle());
        assertEquals(recipe.getDescription(), shortInfo.getDescription());
        assertEquals(recipe.getImage(), shortInfo.getImage());
        assertEquals(recipe.getCookTime(), shortInfo.getCookTime());
        assertEquals(recipe.getServings(), shortInfo.getServings());
    }

    @Test
    void mapCommentToCommentByRecipe_ShouldMapCorrectly() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("testUser")
                .email("test@example.com")
                .profilePicture("profile.jpg")
                .dateRegistered(LocalDateTime.now())
                .dateUpdated(LocalDateTime.now())
                .isActive(true)
                .role(Role.USER)
                .build();

        Comment comment = Comment.builder()
                .id(UUID.randomUUID())
                .content("Nice recipe!")
                .creator(user)
                .createdDate(LocalDateTime.now())
                .build();

        CommentByRecipe commentByRecipe = DtoMapper.mapCommentToCommentByRecipe(comment);

        assertNotNull(commentByRecipe);
        assertEquals(comment.getId(), commentByRecipe.getId());
        assertEquals(comment.getContent(), commentByRecipe.getContent());
        assertEquals(comment.getCreator().getUsername(), commentByRecipe.getCreatedBy());
        assertEquals(comment.getCreatedDate(), commentByRecipe.getCreatedDate());
    }

    @Test
    void mapCategoryToCategoryShort_ShouldMapCorrectly() {
        Category category = Category.builder()
                .id(UUID.randomUUID())
                .imageUrl("image.jpg")
                .description("Sweet treats")
                .recipes(null)
                .name(CategoryName.MAIN_COURSE)
                .build();

        CategoryShort categoryShort = DtoMapper.mapCategoryToCategoryShort(category);

        assertNotNull(categoryShort);
        assertEquals(category.getId(), categoryShort.getId());
        assertEquals(category.getName(), categoryShort.getName());
        assertEquals(category.getImageUrl(), categoryShort.getImageUrl());
    }

    @Test
    void mapRecipeToEditRecipe_ShouldMapCorrectly() {
        Category category = Category.builder()
                .id(UUID.randomUUID())
                .imageUrl("image.jpg")
                .description("Sweet treats")
                .recipes(null)
                .name(CategoryName.MAIN_COURSE)
                .build();

        Recipe recipe = Recipe.builder()
                .id(UUID.randomUUID())
                .title("title")
                .description("description")
                .ingredients(List.of("Flour", "Sugar"))
                .instructions("Mix and bake")
                .categories(List.of(category))
                .image("image.jpg")
                .cookTime(30)
                .prepTime(4)
                .build();

        EditRecipe editRecipe = DtoMapper.mapRecipeToEditRecipe(recipe);

        List<CategoryName> expectedCategories = recipe.getCategories().stream().map(Category::getName).toList();

        assertNotNull(editRecipe);
        assertEquals(recipe.getId(), editRecipe.getId());
        assertEquals(recipe.getTitle(), editRecipe.getTitle());
        assertEquals(recipe.getDescription(), editRecipe.getDescription());
        assertEquals(String.join(",", recipe.getIngredients()), editRecipe.getIngredients());
        assertEquals(recipe.getInstructions(), editRecipe.getInstructions());
        assertEquals(expectedCategories, editRecipe.getCategories());
        assertEquals(recipe.getCookTime(), editRecipe.getCookTime());
        assertEquals(recipe.getServings(), editRecipe.getServings());
    }
}
