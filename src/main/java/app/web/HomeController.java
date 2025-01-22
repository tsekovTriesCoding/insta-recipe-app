package app.web;

import app.category.model.Category;
import app.category.service.CategoryService;
import app.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class HomeController {
    private final UserService userService;
    private final CategoryService categoryService;

    @GetMapping("/")
    public String getIndexPage() {
        return "index";
    }

    @GetMapping("/home")
    public String getHomePage(Model model) {
//
//        UUID userId = (UUID) session.getAttribute(USER_ID_SESSION_ATTRIBUTE);
//        User user = userService.getById(userId);

//        modelAndView.addObject("user", user);

        List<Category> categories = categoryService.getAll();
        model.addAttribute("categories", categories);

        return "/home";
    }
}
