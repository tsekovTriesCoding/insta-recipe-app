package app.web;

import app.category.model.Category;
import app.category.model.CategoryName;
import app.category.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CategoryControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setName(CategoryName.DESSERTS);
        testCategory.setImageUrl("test-image.png");
        categoryRepository.save(testCategory);
    }

    @WithMockUser(username = "testUser")
    @Test
    void testCategoryView_ShouldReturnCategoryPage() throws Exception {
        mockMvc.perform(get("/categories/" + testCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("category"))
                .andExpect(model().attributeExists("categoryDetails"))
                .andExpect(model().attribute("categoryDetails", hasProperty("name", equalTo("Desserts"))));
    }

    @WithMockUser(username = "testUser")
    @Test
    void testCategoryView_WhenCategoryDoesNotExist_ShouldReturnErrorPage() throws Exception {
        UUID nonExistingCategoryId = UUID.randomUUID();

        mockMvc.perform(get("/categories/{id}", nonExistingCategoryId))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error-page"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", containsString("Category with id " + nonExistingCategoryId + " not found")));
    }
}
