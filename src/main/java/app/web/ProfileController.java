package app.web;

import app.user.service.UserDetailsServiceImpl;
import app.user.service.UserService;
import app.web.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
@RequestMapping("/my-profile")
public class ProfileController {

    private final UserService userService;
    private final UserDetailsServiceImpl userDetailsService;

    @ModelAttribute
    public ChangeProfilePicture changeProfilePicture() {
        return new ChangeProfilePicture();
    }

    @ModelAttribute
    public ChangeUsername changeUsername() {
        return new ChangeUsername();
    }

    @ModelAttribute
    public ChangeEmail changeEmail() {
        return new ChangeEmail();
    }

    @ModelAttribute
    public ChangePassword changePassword() {
        return new ChangePassword();
    }

    @GetMapping()
    public String getMyProfilePage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        UserProfileInfo userProfileInfo = userService.getUserProfileInfo(userDetails.getUsername());

        model.addAttribute("userProfileInfo", userProfileInfo);

        return "profile";
    }

    @PutMapping("/{id}/change-picture")
    public String changeMyProfilePicture(@PathVariable UUID id,
                                         @Valid ChangeProfilePicture changeProfilePicture,
                                         BindingResult bindingResult,
                                         RedirectAttributes redirectAttributes,
                                         Model model,
                                         @AuthenticationPrincipal UserDetails userDetails) throws IOException {

        if (bindingResult.hasErrors()) {
            UserProfileInfo userProfileInfo = userService.getUserProfileInfo(userDetails.getUsername());

            model.addAttribute("userProfileInfo", userProfileInfo);
            model.addAttribute("openPictureModal", true);

            return "profile";
        }

        UserProfileInfo userProfileInfo = userService.updateProfilePicture(id, changeProfilePicture.getProfilePicture());
        redirectAttributes.addFlashAttribute("userProfileInfo", userProfileInfo);
        redirectAttributes.addFlashAttribute("success", "Profile picture updated successfully");
        return "redirect:/my-profile";
    }

    @PutMapping("/{id}/change-username")
    public String changeMyProfileUsername(@PathVariable UUID id,
                                          @Valid ChangeUsername changeUsername,
                                          BindingResult bindingResult,
                                          RedirectAttributes redirectAttributes,
                                          Model model,
                                          @AuthenticationPrincipal UserDetails userDetails) {

        if (bindingResult.hasErrors()) {
            UserProfileInfo userProfileInfo = userService.getUserProfileInfo(userDetails.getUsername());

            model.addAttribute("userProfileInfo", userProfileInfo);
            model.addAttribute("openUsernameModal", true);

            return "profile";
        }

        UserProfileInfo userProfileInfo = userService.updateUsername(id, changeUsername.getUsername());

        updateAuthentication(changeUsername.getUsername(), SecurityContextHolder.getContext().getAuthentication());

        redirectAttributes.addFlashAttribute("userProfileInfo", userProfileInfo);
        redirectAttributes.addFlashAttribute("success", "Username updated successfully");

        return "redirect:/my-profile";
    }

    @PutMapping("/{id}/change-email")
    public String changeMyProfileEmail(@PathVariable UUID id,
                                       @Valid ChangeEmail changeEmail,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes,
                                       Model model,
                                       @AuthenticationPrincipal UserDetails userDetails) {

        if (bindingResult.hasErrors()) {
            UserProfileInfo userProfileInfo = userService.getUserProfileInfo(userDetails.getUsername());

            model.addAttribute("userProfileInfo", userProfileInfo);
            model.addAttribute("openEmailModal", true);

            return "profile";
        }

        UserProfileInfo userProfileInfo = userService.updateEmail(id, changeEmail.getEmail());
        redirectAttributes.addFlashAttribute("userProfileInfo", userProfileInfo);
        redirectAttributes.addFlashAttribute("success", "Email updated successfully");

        return "redirect:/my-profile";
    }

    @PutMapping("/{id}/change-password")
    public String changeMyProfilePassword(@PathVariable UUID id,
                                          @Valid ChangePassword changePassword,
                                          BindingResult bindingResult,
                                          RedirectAttributes redirectAttributes,
                                          Model model,
                                          @AuthenticationPrincipal UserDetails userDetails) {

        if (bindingResult.hasErrors()) {
            UserProfileInfo userProfileInfo = userService.getUserProfileInfo(userDetails.getUsername());

            model.addAttribute("userProfileInfo", userProfileInfo);
            model.addAttribute("openPasswordModal", true);

            return "profile";
        }

        UserProfileInfo userProfileInfo = userService.updatePassword(id, changePassword.getPassword());
        redirectAttributes.addFlashAttribute("userProfileInfo", userProfileInfo);
        redirectAttributes.addFlashAttribute("success", "Password updated successfully");

        return "redirect:/my-profile";
    }

    private void updateAuthentication(String username, Object credentials) {
        UserDetails updatedUserDetails = userDetailsService.loadUserByUsername(username);
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                updatedUserDetails, credentials, updatedUserDetails.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }
}