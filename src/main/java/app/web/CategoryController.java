package app.web;

import app.category.model.Category;
import app.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/{name}")
    public String categoryView(Model model, @PathVariable String name) {
        List<Category> categories = categoryService.getAll();

        model.addAttribute("categories", categories);
        return "categories";
    }
}
