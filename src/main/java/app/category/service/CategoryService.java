package app.category.service;

import app.category.model.Category;
import app.category.model.CategoryName;
import app.category.repository.CategoryRepository;
import app.recipe.model.Recipe;
import app.recipe.service.RecipeService;
import app.web.dto.CategoryDetails;
import app.web.dto.CategoryShort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryShort> getAll() {
        return categoryRepository.findAll()
                .stream()
                .map(category -> CategoryShort.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .build()).toList();
    }

    public CategoryDetails getById(UUID id) {
        return categoryRepository.findById(id)
                .map(category -> CategoryDetails.builder()
                        .id(category.getId())
                        .name(category.getName().getValue())
                        .description(category.getDescription())
                        .recipes(category.getRecipes())
                        .build())
                .orElseThrow(NoSuchElementException::new);
    }

    public Category getByName(CategoryName categoryName) {
        return categoryRepository.findByName(categoryName).orElseThrow(NoSuchElementException::new);
    }

    public void update(Category categoryToEdit, Recipe recipeToUpdate) {
        if (recipeToUpdate.getCategories().contains(categoryToEdit)) {
            categoryToEdit.getRecipes().remove(recipeToUpdate);
            recipeToUpdate.getCategories().remove(categoryToEdit);
        } else {
            categoryToEdit.getRecipes().add(recipeToUpdate);
            recipeToUpdate.getCategories().add(categoryToEdit);
        }

        categoryRepository.save(categoryToEdit);
    }
}
