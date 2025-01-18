package app.web;

import app.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@Controller
public class HomeController {
    private final UserService userService;

    @GetMapping("/")
    public String getIndexPage() {
        return "index";
    }

    @GetMapping("/home")
    public String getHomePage(Model model, HttpSession session) {
//
//        UUID userId = (UUID) session.getAttribute(USER_ID_SESSION_ATTRIBUTE);
//        User user = userService.getById(userId);

//        modelAndView.addObject("user", user);

        return "/home";
    }
}
