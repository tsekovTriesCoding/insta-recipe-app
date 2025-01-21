package app.web;

import app.user.model.User;
import app.user.service.UserDetailsServiceImpl;
import app.user.service.UserService;
import app.web.dto.UserProfileInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
@RequestMapping("/my-profile")
public class ProfileController {

    private final UserService userService;
    private final UserDetailsServiceImpl userDetailsService;

    @GetMapping()
    public String getMyProfilePage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        UserProfileInfo userProfileInfo = userService.getUserProfileInfo(userDetails.getUsername());

        if (!model.containsAttribute("userProfileInfo")) {
            model.addAttribute("userProfileInfo", userProfileInfo);
        }

        return "profile";
    }

    @PutMapping("/{id}/change-picture")
    public String changeMyProfilePicture(@PathVariable UUID id,
                                         @RequestParam("profilePicture") MultipartFile file,
                                         Model model) throws IOException {

        UserProfileInfo userProfileInfo = userService.updateProfilePicture(id, file);
        model.addAttribute("userProfileInfo", userProfileInfo);

        return "redirect:/my-profile";
    }

    @PutMapping("/{id}/change-username")
    public String changeMyProfileUsername(@PathVariable UUID id,
                                          @RequestParam("username") String username,
                                          Model model) {

        UserProfileInfo userProfileInfo = userService.updateUsername(id, username);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails updatedUserDetails = userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                updatedUserDetails, authentication.getCredentials(), updatedUserDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        model.addAttribute("userProfileInfo", userProfileInfo);

        return "redirect:/my-profile";
    }
}
