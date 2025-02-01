package app.web;

import app.exception.UserAlreadyExistsException;
import app.exception.UserNotFoundException;
import app.user.service.UserDetailsServiceImpl;
import app.user.service.UserService;
import app.web.dto.*;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.servlet.ModelAndView;
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
    public ModelAndView changeMyProfilePicture(@PathVariable UUID id,
                                               @Valid ChangeProfilePicture changeProfilePicture,
                                               BindingResult bindingResult,
                                               RedirectAttributes redirectAttributes) throws IOException {

        if (bindingResult.hasErrors()) {
            UserProfileInfo userProfileInfo = userService.getUserProfileInfoById(id);

            ModelAndView modelAndView = new ModelAndView("profile", "userProfileInfo", userProfileInfo);
            modelAndView.addObject("changeProfilePicture", changeProfilePicture);
            modelAndView.addObject("openPictureModal", true);
            return modelAndView;
        }

        UserProfileInfo userProfileInfo = userService.updateProfilePicture(id, changeProfilePicture.getProfilePicture());
        ModelAndView modelAndView = new ModelAndView("redirect:/my-profile");
        redirectAttributes.addFlashAttribute("success", "Profile picture updated successfully");

        return modelAndView;
    }

    @PutMapping("/{id}/change-username")
    public ModelAndView changeMyProfileUsername(@PathVariable UUID id,
                                                @Valid ChangeUsername changeUsername,
                                                BindingResult bindingResult,
                                                RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            UserProfileInfo userProfileInfo = userService.getUserProfileInfoById(id);

            ModelAndView modelAndView = new ModelAndView("profile", "userProfileInfo", userProfileInfo);
            modelAndView.addObject("changeUsername", changeUsername);
            modelAndView.addObject("openUsernameModal", true);
            return modelAndView;
        }

        UserProfileInfo userProfileInfo = userService.updateUsername(id, changeUsername.getUsername());
        updateAuthentication(changeUsername.getUsername(), SecurityContextHolder.getContext().getAuthentication());

        ModelAndView modelAndView = new ModelAndView("redirect:/my-profile");
        redirectAttributes.addFlashAttribute("success", "Username updated successfully");

        return modelAndView;
    }

    @PutMapping("/{id}/change-email")
    public ModelAndView changeMyProfileEmail(@PathVariable UUID id,
                                             @Valid ChangeEmail changeEmail,
                                             BindingResult bindingResult,
                                             RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            UserProfileInfo userProfileInfo = userService.getUserProfileInfoById(id);

            ModelAndView modelAndView = new ModelAndView("profile", "userProfileInfo", userProfileInfo);
            modelAndView.addObject("changeEmail", changeEmail);
            modelAndView.addObject("openEmailModal", true);
            return modelAndView;
        }

        UserProfileInfo userProfileInfo = userService.updateEmail(id, changeEmail.getEmail());
        ModelAndView modelAndView = new ModelAndView("redirect:/my-profile");
        redirectAttributes.addFlashAttribute("success", "Email updated successfully");

        return modelAndView;
    }

    @PutMapping("/{id}/change-password")
    public ModelAndView changeMyProfilePassword(@PathVariable UUID id,
                                                @Valid ChangePassword changePassword,
                                                BindingResult bindingResult,
                                                RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            UserProfileInfo userProfileInfo = userService.getUserProfileInfoById(id);

            ModelAndView modelAndView = new ModelAndView("profile", "userProfileInfo", userProfileInfo);
            modelAndView.addObject("changePassword", changePassword);
            modelAndView.addObject("openPasswordModal", true);
            return modelAndView;
        }

        UserProfileInfo userProfileInfo = userService.updatePassword(id, changePassword.getPassword());
        ModelAndView modelAndView = new ModelAndView("redirect:/my-profile");
        redirectAttributes.addFlashAttribute("success", "Password updated successfully");

        return modelAndView;
    }

    private void updateAuthentication(String username, Object credentials) {
        UserDetails updatedUserDetails = userDetailsService.loadUserByUsername(username);
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                updatedUserDetails, credentials, updatedUserDetails.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public String handleUserNotFoundException(UserNotFoundException ex,
                                              Model model) {

        model.addAttribute("error", ex.getMessage());
        return "error-page";
    }
}