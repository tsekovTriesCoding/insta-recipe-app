package app.web;

import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.RegisterRequest;
import app.web.dto.UserProfileInfo;
import app.web.dto.UserUpdate;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;

@RequiredArgsConstructor
@Controller
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/login")
    public ModelAndView getLoginPage(HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("login");

        String error = (String) request.getSession().getAttribute("error");
        if (error != null) {
            modelAndView.addObject("error", error);
            request.getSession().removeAttribute("error");
        }

        return modelAndView;
    }

    @GetMapping("/register")
    public ModelAndView getRegisterPage() {
        ModelAndView modelAndView = new ModelAndView("register");

        modelAndView.addObject("registerRequest", new RegisterRequest());

        return modelAndView;
    }

    @PostMapping("/register")
    public ModelAndView register(@Valid RegisterRequest registerRequest,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("registerRequest", registerRequest);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.registerRequest", bindingResult);
            return new ModelAndView("register");
        }

        User registeredUser = userService.register(registerRequest);

        return new ModelAndView("redirect:/home");
    }

    @GetMapping("/profile")
    public ModelAndView getMyProfilePage(Principal principal) {
        ModelAndView modelAndView = new ModelAndView("profile");

        UserProfileInfo userProfileInfo = userService.getByUsername(principal.getName());

        modelAndView.addObject("userProfileInfo", userProfileInfo);
        return modelAndView;
    }

    @PutMapping("/profile")
    public ModelAndView updateProfile(UserProfileInfo userProfileInfo,
                                      @RequestParam("profilePicture") MultipartFile file,
                                      Principal principal) throws IOException {
        ModelAndView modelAndView = new ModelAndView("profile");

        UserProfileInfo update = userService.update(userProfileInfo, file,principal.getName());

        modelAndView.addObject("userProfileInfo", update);

        return modelAndView;
    }
}
