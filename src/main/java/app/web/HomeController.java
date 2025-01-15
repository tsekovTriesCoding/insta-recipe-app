package app.web;

import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.LoginRequest;
import app.web.dto.RegisterRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.UUID;

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
