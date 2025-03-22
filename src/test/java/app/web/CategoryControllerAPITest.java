package app.web;

import app.category.model.Category;
import app.category.service.CategoryService;
import app.exception.CategoryNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static app.TestBuilder.aRandomCategory;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@WithMockUser
public class CategoryControllerAPITest {

    @MockitoBean
    private CategoryService categoryService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testCategoryView_ShouldReturnCategoryPage() throws Exception {
        Category category = aRandomCategory();

        when(categoryService.getById(category.getId())).thenReturn(category);

        mockMvc.perform(get("/categories/" + category.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("category"))
                .andExpect(model().attributeExists("categoryDetails"))
                .andExpect(model().attribute("categoryDetails", hasProperty("name", equalTo("Main Course"))));
    }

    @Test
    void testCategoryView_WhenCategoryDoesNotExist_ShouldReturnErrorPage() throws Exception {
        UUID nonExistingCategoryId = UUID.randomUUID();

        when(categoryService.getById(nonExistingCategoryId)).thenThrow(new CategoryNotFoundException("Category with id " + nonExistingCategoryId + " not found"));

        mockMvc.perform(get("/categories/{id}", nonExistingCategoryId))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error-page"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", containsString("Category with id " + nonExistingCategoryId + " not found")));
    }
}
