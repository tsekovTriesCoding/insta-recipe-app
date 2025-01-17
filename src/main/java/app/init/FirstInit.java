package app.init;

import app.category.model.Category;
import app.category.model.CategoryName;
import app.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class FirstInit implements CommandLineRunner {
    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) throws Exception {
        if(categoryRepository.count() == 0) {
            for (CategoryName name : CategoryName.values()) {
                Category category = new Category();
                category.setName(name);
                category.setDescription("Random Description");
                categoryRepository.save(category);
            }
        }
    }
}
