package app.category.service;

import app.category.repository.CategoryRepository;
import app.web.dto.CategoryShort;
import app.web.dto.CategoryDetails;
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
}
