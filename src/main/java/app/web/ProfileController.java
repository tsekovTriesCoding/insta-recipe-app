package app.web;

import app.user.service.UserService;
import app.web.dto.UserProfileInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@RequiredArgsConstructor
@Controller
@RequestMapping("/my-profile")
public class ProfileController {

    private final UserService userService;

    @GetMapping()
    public ModelAndView getMyProfilePage(@AuthenticationPrincipal UserDetails userDetails) {
        ModelAndView modelAndView = new ModelAndView("profile");

        UserProfileInfo userProfileInfo = userService.getByUsername(userDetails.getUsername());

        modelAndView.addObject("userProfileInfo", userProfileInfo);
        return modelAndView;
    }
}
