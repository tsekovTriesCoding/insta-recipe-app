package app.web;

import app.category.model.CategoryName;
import app.category.service.CategoryService;
import app.web.dto.CategoryShort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class HomeController {
    
    private final CategoryService categoryService;

    @GetMapping("/")
    public String getIndexPage() {
        return "index";
    }

    @GetMapping("/home")
    public String getHomePage(Model model) {

        List<CategoryShort> categories = categoryService.getAll();
        model.addAttribute("categories", categories);

        return "/home";
    }
}
