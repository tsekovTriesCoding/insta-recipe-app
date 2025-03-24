package app;

import app.category.model.Category;
import app.category.model.CategoryName;
import app.comment.model.Comment;
import app.recipe.model.Recipe;
import app.user.model.Role;
import app.user.model.User;
import app.web.dto.AddRecipe;
import app.web.dto.EditRecipe;
import app.web.dto.RecipeDetails;
import app.web.dto.RegisterRequest;
import lombok.experimental.UtilityClass;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@UtilityClass
public class TestBuilder {

    public static User aRandomUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .username("username")
                .email("email")
                .password("password")
                .profilePicture("/images/default-profile.png")
                .dateRegistered(LocalDateTime.now())
                .role(Role.USER)
                .isActive(true)
                .build();
    }

    public static User aRandomWithoutId() {
        return User.builder()
                .username("username")
                .email("email")
                .password("password")
                .profilePicture("/images/default-profile.png")
                .dateRegistered(LocalDateTime.now())
                .role(Role.USER)
                .isActive(true)
                .build();
    }

    public static RegisterRequest aRandomRegisterRequest() {
        return RegisterRequest.builder()
                .username("username")
                .email("email")
                .password("password")
                .confirmPassword("password")
                .build();
    }

    public static Recipe aRandomRecipe() {
        return Recipe.builder()
                .id(UUID.randomUUID())
                .title("title")
                .description("description")
                .ingredients(List.of("tomato", "cucumber"))
                .instructions("Instructions")
                .createdDate(LocalDateTime.now())
                .cookTime(2)
                .prepTime(3)
                .servings(4)
                .image("imageUrl")
                .comments(new ArrayList<>())
                .likes(new ArrayList<>())
                .build();
    }

    public static Recipe aRandomRecipeWithoutId() {
        return Recipe.builder()
                .title("title")
                .description("description")
                .ingredients(List.of("tomato", "cucumber"))
                .instructions("Instructions")
                .createdDate(LocalDateTime.now())
                .cookTime(2)
                .prepTime(3)
                .servings(4)
                .image("imageUrl")
                .comments(new ArrayList<>())
                .likes(new ArrayList<>())
                .build();
    }

    public static AddRecipe aRandomAddRecipe() {
        MockMultipartFile image = new MockMultipartFile("image", "image.jpg", "image/jpg", new byte[1]);

        AddRecipe addRecipe = new AddRecipe();
        addRecipe.setTitle("title");
        addRecipe.setDescription("description");
        addRecipe.setImage(image);
        addRecipe.setInstructions("Instructions");
        addRecipe.setIngredients("ingredient1, ingredient2");
        addRecipe.setCategories(List.of(CategoryName.MAIN_COURSE));
        addRecipe.setCookTime(1);
        addRecipe.setPrepTime(2);
        addRecipe.setServings(3);

        return addRecipe;
    }

    public static EditRecipe aRandomEditRecipe() {
        return EditRecipe.builder()
                .id(UUID.randomUUID())
                .title("title")
                .description("description")
                .instructions("Instructions")
                .categories(List.of(CategoryName.MAIN_COURSE))
                .ingredients("ingredient1, ingredient2")
                .cookTime(2)
                .prepTime(3)
                .servings(4)
                .build();
    }

    public static Comment aRandomComment() {
        return Comment.builder()
                .id(UUID.randomUUID())
                .content("content")
                .createdDate(LocalDateTime.now())
                .build();

    }

    public static Category aRandomCategory() {
        return Category.builder()
                .id(UUID.randomUUID())
                .name(CategoryName.MAIN_COURSE)
                .imageUrl("imageUrl")
                .build();
    }

    public static Category aRandomCategoryWithoutId() {
        return Category.builder()
                .name(CategoryName.MAIN_COURSE)
                .imageUrl("imageUrl")
                .build();
    }
}
