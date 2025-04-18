package app.web;

import app.activitylog.dto.ActivityLogResponse;
import app.activitylog.service.ActivityLogService;
import app.mapper.DtoMapper;
import app.security.CustomUserDetails;
import app.user.model.User;
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
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
@RequestMapping("/my-profile")
public class ProfileController {

    private final UserService userService;
    private final ActivityLogService activityLogService;

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
    public String getMyProfilePage(@AuthenticationPrincipal CustomUserDetails customUserDetails, Model model) {
        User user = userService.getUserById(customUserDetails.getId());
        UserProfileInfo userProfileInfo = DtoMapper.mapUserToUserProfileInfo(user);

        model.addAttribute("userProfileInfo", userProfileInfo);

        return "profile";
    }

    @PutMapping("/{id}/change-picture")
    public ModelAndView changeMyProfilePicture(@PathVariable UUID id,
                                               @Valid ChangeProfilePicture changeProfilePicture,
                                               BindingResult bindingResult,
                                               RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            User user = userService.getUserById(id);
            UserProfileInfo userProfileInfo = DtoMapper.mapUserToUserProfileInfo(user);

            ModelAndView modelAndView = new ModelAndView("profile", "userProfileInfo", userProfileInfo);
            modelAndView.addObject("changeProfilePicture", changeProfilePicture);
            modelAndView.addObject("openPictureModal", true);
            return modelAndView;
        }

        userService.updateProfilePicture(id, changeProfilePicture.getProfilePicture());
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
            User user = userService.getUserById(id);
            UserProfileInfo userProfileInfo = DtoMapper.mapUserToUserProfileInfo(user);

            ModelAndView modelAndView = new ModelAndView("profile", "userProfileInfo", userProfileInfo);
            modelAndView.addObject("changeUsername", changeUsername);
            modelAndView.addObject("openUsernameModal", true);
            return modelAndView;
        }

        userService.updateUsername(id, changeUsername.getUsername());
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
            User user = userService.getUserById(id);
            UserProfileInfo userProfileInfo = DtoMapper.mapUserToUserProfileInfo(user);

            ModelAndView modelAndView = new ModelAndView("profile", "userProfileInfo", userProfileInfo);
            modelAndView.addObject("changeEmail", changeEmail);
            modelAndView.addObject("openEmailModal", true);
            return modelAndView;
        }

        userService.updateEmail(id, changeEmail.getEmail());
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
            User user = userService.getUserById(id);
            UserProfileInfo userProfileInfo = DtoMapper.mapUserToUserProfileInfo(user);

            ModelAndView modelAndView = new ModelAndView("profile", "userProfileInfo", userProfileInfo);
            modelAndView.addObject("changePassword", changePassword);
            modelAndView.addObject("openPasswordModal", true);
            return modelAndView;
        }

        userService.updatePassword(id, changePassword.getPassword());
        ModelAndView modelAndView = new ModelAndView("redirect:/my-profile");
        redirectAttributes.addFlashAttribute("success", "Password updated successfully");

        return modelAndView;
    }

    @GetMapping("/{id}/activity-log")
    public ModelAndView activityLog(@PathVariable UUID id) {
        ModelAndView modelAndView = new ModelAndView("activity-log");

        List<ActivityLogResponse> activityLog = activityLogService.getActivityLog(id);

        modelAndView.addObject("activityLog", activityLog);
        modelAndView.addObject("userId", id);

        return modelAndView;
    }

    @DeleteMapping("/{id}/activity-log/clear")
    public ModelAndView clearActivityLog(@PathVariable UUID id) {
        ModelAndView modelAndView = new ModelAndView("redirect:/my-profile/" + id + "/activity-log");

        activityLogService.deleteLogsByUserId(id);

        return modelAndView;
    }

    private void updateAuthentication(String username, Object credentials) {
        UserDetails updatedUserDetails = userService.loadUserByUsername(username);
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                updatedUserDetails, credentials, updatedUserDetails.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }
}