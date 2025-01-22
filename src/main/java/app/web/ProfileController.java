package app.web;

import app.user.service.UserDetailsServiceImpl;
import app.user.service.UserService;
import app.web.dto.ChangeEmail;
import app.web.dto.ChangeProfilePicture;
import app.web.dto.UserProfileInfo;
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
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

        model.addAttribute("userProfileInfo", userProfileInfo);

        return "profile";
    }

    @PutMapping("/{id}/change-picture")
    public String changeMyProfilePicture(@PathVariable UUID id,
                                         @Valid ChangeProfilePicture changeProfilePicture,
                                         BindingResult bindingResult,
                                         RedirectAttributes redirectAttributes) throws IOException {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors().get(0).getDefaultMessage());
            redirectAttributes.addFlashAttribute("openPictureModal", true);
            return "redirect:/my-profile";
        }

        UserProfileInfo userProfileInfo = userService.updateProfilePicture(id, changeProfilePicture.getProfilePicture());
        redirectAttributes.addFlashAttribute("userProfileInfo", userProfileInfo);
        redirectAttributes.addFlashAttribute("success", "Profile picture updated successfully");
        return "redirect:/my-profile";
    }

    @PutMapping("/{id}/change-username")
    public String changeMyProfileUsername(@PathVariable UUID id,
                                          @RequestParam("username") String username,
                                          RedirectAttributes redirectAttributes) {

        if (userService.existsByUsername(username)) {
            redirectAttributes.addFlashAttribute("error", "Username already exists");
            redirectAttributes.addFlashAttribute("openModal", true);
            return "redirect:/my-profile";
        }

        if (username.length() < 5) {
            redirectAttributes.addFlashAttribute("error", "Username must be at least 5 symbols");
            redirectAttributes.addFlashAttribute("openModal", true);
            return "redirect:/my-profile";
        }

        UserProfileInfo userProfileInfo = userService.updateUsername(id, username);
        updateAuthentication(username, SecurityContextHolder.getContext().getAuthentication());
        redirectAttributes.addFlashAttribute("userProfileInfo", userProfileInfo);
        redirectAttributes.addFlashAttribute("success", "Username updated successfully");

        return "redirect:/my-profile";
    }

    @PutMapping("/{id}/change-email")
    public String changeMyProfileEmail(@PathVariable UUID id,
                                       @Valid ChangeEmail changeEmail,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors().get(0).getDefaultMessage());
            redirectAttributes.addFlashAttribute("changeEmailModal", true);
            return "redirect:/my-profile";
        }

        UserProfileInfo userProfileInfo = userService.updateEmail(id, changeEmail.getEmail());
        redirectAttributes.addFlashAttribute("userProfileInfo", userProfileInfo);
        redirectAttributes.addFlashAttribute("success", "Email updated successfully");

        return "redirect:/my-profile";
    }

    @ControllerAdvice
    public static class FileUploadExceptionAdvice {

        @ExceptionHandler(MaxUploadSizeExceededException.class)
        public String handleMaxSizeException(MaxUploadSizeExceededException e, RedirectAttributes redirectAttributes) {
            redirectAttributes.addFlashAttribute("error", "File size exceeds the 3MB limit.");
            redirectAttributes.addFlashAttribute("openModal", true);
            return "redirect:/my-profile";
        }
    }

    private void updateAuthentication(String username, Object credentials) {
        UserDetails updatedUserDetails = userDetailsService.loadUserByUsername(username);
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                updatedUserDetails, credentials, updatedUserDetails.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }
}