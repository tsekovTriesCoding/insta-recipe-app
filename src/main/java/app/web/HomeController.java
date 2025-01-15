package app.web;

import app.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@RequiredArgsConstructor
@Controller
public class HomeController {
    private final UserService userService;

    @GetMapping("/")
    public String getIndexPage() {
        return "index";
    }

    @GetMapping("/home")
    public ModelAndView getHomePage(HttpSession session) {
//
//        UUID userId = (UUID) session.getAttribute(USER_ID_SESSION_ATTRIBUTE);
//        User user = userService.getById(userId);

        ModelAndView modelAndView = new ModelAndView("home");
//        modelAndView.addObject("user", user);

        return modelAndView;
    }
}
