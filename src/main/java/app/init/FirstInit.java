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
        if (categoryRepository.count() == 7) {
            Category category = new Category();
            category.setName(CategoryName.SOUPS);
            category.setDescription("Soups are comforting and versatile dishes that can be enjoyed as a starter, main course, or even a light meal. They come in various forms, including broths, purees, and chunky stews, catering to diverse culinary traditions worldwide. Common ingredients include vegetables, meats, grains, and legumes, often simmered to develop rich flavors. Soups can be served hot or cold, with popular varieties like chicken noodle, minestrone, and gazpacho highlighting their adaptability. Whether nourishing or refreshing, soups provide a warm embrace in every bowl, making them a staple in many cuisines.");
            category.setImageUrl("/images/soups.jpg");
            categoryRepository.save(category);
        }
    }
}
