package app.web;

import app.user.service.UserDetailsServiceImpl;
import app.user.service.UserService;
import app.web.dto.UserProfileInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
                                         @RequestParam("profilePicture") MultipartFile file,
                                         RedirectAttributes redirectAttributes) throws IOException {

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "You must choose a file");
            redirectAttributes.addFlashAttribute("openPictureModal", true);
            return "redirect:/my-profile";
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/png") && !contentType.equals("image/jpeg") && !contentType.equals("image/jpg"))) {
            redirectAttributes.addFlashAttribute("error", "Invalid file type. Only PNG, JPEG, and JPG are allowed.");
            redirectAttributes.addFlashAttribute("openPictureModal", true);
            return "redirect:/my-profile";
        }

        if (file.getSize() > 3 * 1024 * 1024) {
            redirectAttributes.addFlashAttribute("error", "File size must not exceed 3MB.");
            redirectAttributes.addFlashAttribute("openPictureModal", true);
            return "redirect:/my-profile";
        }

        UserProfileInfo userProfileInfo = userService.updateProfilePicture(id, file);
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

    //TODO: do the controller advice when the file exceed 10MB to return a custom error page
//    @ControllerAdvice
//    public class MyControllerAdvice {
//
//        @ExceptionHandler(MaxUploadSizeExceededException.class)
//        public ResponseEntity<String> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
//            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
//                    .body("File size exceeded. Maximum allowed size is " + ex.getMaxUploadSize());
//        }
//    }

    private void updateAuthentication(String username, Object credentials) {
        UserDetails updatedUserDetails = userDetailsService.loadUserByUsername(username);
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                updatedUserDetails, credentials, updatedUserDetails.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }
}
