package app.web;

import app.activitylog.dto.ActivityLogResponse;
import app.activitylog.service.ActivityLogService;
import app.security.CustomUserDetails;
import app.user.model.Role;
import app.user.model.User;
import app.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static app.TestBuilder.aRandomUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
public class ProfileControllerAPITest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ActivityLogService activityLogService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetProfilePage_ShouldReturnProfilePage() throws Exception {
        CustomUserDetails principal = new CustomUserDetails(UUID.randomUUID(), "user", "pass", Role.USER, true);

        User user = aRandomUser();

        when(userService.getUserById(any())).thenReturn(user);

        mockMvc.perform(get("/my-profile")
                        .with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("userProfileInfo"));
    }

    @Test
    @WithMockUser
    void testChangeMyProfilePicture_ShouldSucceed() throws Exception {
        MockMultipartFile profilePicture = new MockMultipartFile(
                "profilePicture", "profile.jpg", "image/jpeg", "test image content".getBytes());

        mockMvc.perform(multipart(HttpMethod.PUT, "/my-profile/" + UUID.randomUUID() + "/change-picture") // Use HttpMethod.PUT
                        .file(profilePicture)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-profile"))
                .andExpect(flash().attribute("success", "Profile picture updated successfully"));
    }

    @Test
    @WithMockUser
    void testChangeProfilePicture_FileEmpty_ShouldReturnError() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile("profilePicture", "profile.jpg", "image/jpeg", new byte[0]);

        User user = aRandomUser();

        when(userService.getUserById(any())).thenReturn(user);

        mockMvc.perform(multipart(HttpMethod.PUT, "/my-profile/" + UUID.randomUUID() + "/change-picture")
                        .file(emptyFile)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("changeProfilePicture"))
                .andExpect(model().attributeExists("openPictureModal"))
                .andExpect(model().attribute("openPictureModal", true));
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    void testChangeProfilePicture_FileTooLarge_ShouldReturnError() throws Exception {
        // Create a large file (3MB+)
        byte[] largeFile = new byte[3 * 1024 * 1024 + 1];
        MockMultipartFile file = new MockMultipartFile("profilePicture", "large-image.jpg", "image/jpeg", largeFile);

        User user = aRandomUser();

        when(userService.getUserById(any())).thenReturn(user);

        mockMvc.perform(multipart(HttpMethod.PUT, "/my-profile/" + UUID.randomUUID() + "/change-picture")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("changeProfilePicture"))
                .andExpect(model().attributeExists("openPictureModal"))
                .andExpect(model().attribute("openPictureModal", true));
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    void testChangeProfilePicture_NonImageFile_ShouldReturnError() throws Exception {
        MockMultipartFile invalidFile = new MockMultipartFile(
                "profilePicture",
                "test.txt",
                "text/plain",
                "This is a text file".getBytes()
        );

        User user = aRandomUser();

        when(userService.getUserById(any())).thenReturn(user);

        mockMvc.perform(multipart(HttpMethod.PUT, "/my-profile/{id}/change-picture", UUID.randomUUID())
                        .file(invalidFile)
                        .with(csrf()))
                .andExpect(status().isOk()) // Should return the profile page, not a redirect
                .andExpect(view().name("profile")) // Should stay on profile page
                .andExpect(model().attributeExists("openPictureModal")) // Modal should be reopened
                .andExpect(model().attributeHasFieldErrors("changeProfilePicture", "profilePicture")); // Error should exist
    }

    @Test
    @WithMockUser
    void testChangeUsername_ShouldChangeUsername() throws Exception {
        CustomUserDetails principal = new CustomUserDetails(UUID.randomUUID(), "user", "pass", Role.USER, true);

        User user = aRandomUser();
        when(userService.getUserById(user.getId())).thenReturn(user);
        when(userService.loadUserByUsername(any())).thenReturn(principal);

        mockMvc.perform(put("/my-profile/" + UUID.randomUUID() + "/change-username")
                        .param("username", "newUsername")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-profile"));
    }

    @Test
    @WithMockUser
    void testChangeUsername_UsernameExists_ShouldReturnError() throws Exception {
        User user = aRandomUser();

        when(userService.getUserById(user.getId())).thenReturn(user);
        when(userService.existsByUsername(user.getUsername())).thenReturn(true);

        mockMvc.perform(put("/my-profile/" + user.getId() + "/change-username")
                        .param("username", user.getUsername())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("changeUsername"))
                .andExpect(model().attributeExists("openUsernameModal"))
                .andExpect(model().attribute("openUsernameModal", true));
    }

    @Test
    @WithMockUser
    void testChangeUsername_UsernameTooShort_ShouldReturnError() throws Exception {
        User user = aRandomUser();

        when(userService.getUserById(user.getId())).thenReturn(user);

        mockMvc.perform(put("/my-profile/{id}/change-username", user.getId())
                        .param("username", "abc") // Simulate form input
                        .with(csrf()))
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("changeUsername"))
                .andExpect(model().attributeExists("openUsernameModal"))
                .andExpect(model().attribute("openUsernameModal", true));
    }

    @Test
    @WithMockUser
    void testChangeEmail_ShouldSucceed() throws Exception {
        User user = aRandomUser();

        when(userService.getUserById(user.getId())).thenReturn(user);

        mockMvc.perform(put("/my-profile/" + user.getId() + "/change-email")
                        .param("email", "new@example.com")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-profile"));
    }

    @Test
    @WithMockUser
    void testChangeEmail_EmailExists_ShouldReturnError() throws Exception {
        User user = aRandomUser();

        when(userService.getUserById(user.getId())).thenReturn(user);
        when(userService.existsByEmail(user.getEmail())).thenReturn(true);

        mockMvc.perform(put("/my-profile/" + user.getId() + "/change-email")
                        .param("email", user.getEmail())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("changeEmail"))
                .andExpect(model().attributeExists("openEmailModal"))
                .andExpect(model().attribute("openEmailModal", true));
    }

    @Test
    @WithMockUser
    void testChangeEmailEmpty() throws Exception {
        User user = aRandomUser();

        when(userService.getUserById(user.getId())).thenReturn(user);

        mockMvc.perform(put("/my-profile/{id}/change-email", user.getId())
                        .param("email", "")
                        .with(csrf()))
                .andExpect(status().isOk()) // Should stay on the profile page
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("changeEmail"))
                .andExpect(model().attributeExists("openEmailModal"))
                .andExpect(model().attribute("openEmailModal", true));
    }

    @Test
    @WithMockUser
    void testChangeEmailInvalidFormat() throws Exception {
        User user = aRandomUser();

        when(userService.getUserById(user.getId())).thenReturn(user);

        mockMvc.perform(put("/my-profile/{id}/change-email", user.getId())
                        .param("email", "invalid-email")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("changeEmail"))
                .andExpect(model().attributeExists("openEmailModal"))
                .andExpect(model().attribute("openEmailModal", true));
    }

    @Test
    @WithMockUser
    void testChangePassword_ShouldSucceed() throws Exception {
        User user = aRandomUser();

        when(userService.getUserById(user.getId())).thenReturn(user);

        mockMvc.perform(put("/my-profile/" + user.getId() + "/change-password")
                        .param("password", "newPassword")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-profile"));
    }

    @Test
    @WithMockUser
    void testChangePassword_LessThan6Characters_ShouldReturnError() throws Exception {
        User user = aRandomUser();

        when(userService.getUserById(user.getId())).thenReturn(user);

        mockMvc.perform(put("/my-profile/" + user.getId() + "/change-password")
                        .param("password", "newPa")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("changePassword"))
                .andExpect(model().attributeExists("openPasswordModal"))
                .andExpect(model().attribute("openPasswordModal", true));
    }

    @Test
    @WithMockUser
    void testActivityLog_ShouldReturnActivityLogPage() throws Exception {
        User user = aRandomUser();

        ActivityLogResponse response1 = ActivityLogResponse.builder()
                .userId(UUID.randomUUID())
                .action("Recipe added")
                .createdOn(LocalDateTime.now())
                .build();

        ActivityLogResponse response2 = ActivityLogResponse.builder()
                .userId(UUID.randomUUID())
                .action("Comment added")
                .createdOn(LocalDateTime.now().plusDays(1))
                .build();

        List<ActivityLogResponse> mockLogs = List.of(response1, response2);

        when(activityLogService.getActivityLog(user.getId())).thenReturn(mockLogs);

        mockMvc.perform(get("/my-profile/{id}/activity-log", user.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("activity-log"))
                .andExpect(model().attributeExists("activityLog"))
                .andExpect(model().attribute("activityLog", mockLogs))
                .andExpect(model().attributeExists("userId"))
                .andExpect(model().attribute("userId", user.getId()));
    }

    @Test
    @WithMockUser
    void testClearActivityLog_ShouldDeleteAllLoggedActivities() throws Exception {
        User user = aRandomUser();

        ActivityLogResponse response1 = ActivityLogResponse.builder()
                .userId(UUID.randomUUID())
                .action("Recipe added")
                .createdOn(LocalDateTime.now())
                .build();

        ActivityLogResponse response2 = ActivityLogResponse.builder()
                .userId(UUID.randomUUID())
                .action("Comment added")
                .createdOn(LocalDateTime.now().plusDays(1))
                .build();

        List<ActivityLogResponse> mockLogs = List.of(response1, response2);

        when(activityLogService.getActivityLog(user.getId())).thenReturn(mockLogs);

        // Perform GET request before deletion to verify logs exist
        mockMvc.perform(get("/my-profile/{id}/activity-log", user.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("activity-log"))
                .andExpect(model().attributeExists("activityLog"))
                .andExpect(model().attribute("activityLog", mockLogs))
                .andExpect(model().attributeExists("userId"))
                .andExpect(model().attribute("userId", user.getId()));

        // Step 2: Mock delete behavior
        doNothing().when(activityLogService).deleteLogsByUserId(user.getId());

        // Perform DELETE request
        mockMvc.perform(delete("/my-profile/{id}/activity-log/clear", user.getId())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-profile/" + user.getId() + "/activity-log"));

        // Step 3: Mock the activity log after deletion (empty)
        Mockito.when(activityLogService.getActivityLog(user.getId())).thenReturn(Collections.emptyList());

        // Perform GET request after deletion to verify logs are empty
        mockMvc.perform(get("/my-profile/{id}/activity-log", user.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("activityLog"))
                .andExpect(model().attribute("activityLog", Collections.emptyList()));
    }
}
