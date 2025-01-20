package app.web;

import app.user.service.UserService;
import app.web.dto.UserProfileInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequiredArgsConstructor
@Controller
@RequestMapping("/my-profile")
public class ProfileController {

    private final UserService userService;

    @GetMapping()
    public String getMyProfilePage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        UserProfileInfo userProfileInfo = userService.getUserProfileInfo(userDetails.getUsername());

        if (!model.containsAttribute("userProfileInfo")) {
            model.addAttribute("userProfileInfo", userProfileInfo);
        }

        return "profile";
    }

    @PutMapping("/picture/change")
    public String changeMyProfilePicture(@AuthenticationPrincipal UserDetails userDetails,
                                         @RequestParam("profilePicture") MultipartFile file,
                                         Model model) throws IOException {

        UserProfileInfo userProfileInfo = userService.updateProfilePicture(userDetails, file);
        model.addAttribute("userProfileInfo", userProfileInfo);

        return "redirect:/my-profile";
    }
}
