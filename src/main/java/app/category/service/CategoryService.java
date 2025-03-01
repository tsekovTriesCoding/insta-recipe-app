package app.category.service;

import app.activity.ActivityLogService;
import app.category.model.Category;
import app.category.model.CategoryName;
import app.category.repository.CategoryRepository;
import app.exception.CategoryNotFoundException;
import app.mapper.DtoMapper;
import app.recipe.model.Recipe;
import app.web.dto.CategoryDetails;
import app.web.dto.CategoryShort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ActivityLogService activityLogService;

    public List<CategoryShort> getAll() {
        return categoryRepository.findAll()
                .stream()
                .map(DtoMapper::mapCategoryToCategoryShort)
                .toList();
    }

    //TODO:handle category not found exception!
    public CategoryDetails getById(UUID id) {
        return categoryRepository.findById(id)
                .map(DtoMapper::mapCategoryToCategoryDetails)
                .orElseThrow(() -> new CategoryNotFoundException("Category with id " + id + " not found"));
    }

    public Category getByName(CategoryName categoryName) {
        return categoryRepository.findByName(categoryName)
                .orElseThrow(() -> new CategoryNotFoundException("Category with name " + categoryName + " not found"));
    }

    public void update(Category categoryToEdit, Recipe recipeToUpdate) {
        if (recipeToUpdate.getCategories().contains(categoryToEdit)) {
            categoryToEdit.getRecipes().remove(recipeToUpdate);
            recipeToUpdate.getCategories().remove(categoryToEdit);

            activityLogService.logActivity("You have successfully remove category %s for recipe %s"
                    .formatted(categoryToEdit.getName(), recipeToUpdate.getTitle()), recipeToUpdate.getCreatedBy().getId());
        } else {
            categoryToEdit.getRecipes().add(recipeToUpdate);
            recipeToUpdate.getCategories().add(categoryToEdit);

            activityLogService.logActivity("You have successfully added category %s for recipe %s"
                    .formatted(categoryToEdit.getName(), recipeToUpdate.getTitle()), recipeToUpdate.getCreatedBy().getId());
        }

        categoryRepository.save(categoryToEdit);
    }
}