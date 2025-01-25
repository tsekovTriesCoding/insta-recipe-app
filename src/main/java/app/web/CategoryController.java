package app.web;

import app.category.service.CategoryService;
import app.web.dto.CategoryDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@RequiredArgsConstructor
@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/{id}")
    public String categoryView(Model model, @PathVariable UUID id) {
        CategoryDetails categoryDetails = categoryService.getById(id);
        model.addAttribute("categoryDetails", categoryDetails);
        return "category";
    }
}
