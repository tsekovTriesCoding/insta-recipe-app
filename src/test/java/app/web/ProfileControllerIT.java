package app.web;

import app.activitylog.dto.ActivityLogResponse;
import app.activitylog.service.ActivityLogService;
import app.cloudinary.dto.ImageUploadResult;
import app.cloudinary.service.CloudinaryService;
import app.security.CustomUserDetails;
import app.user.model.Role;
import app.user.model.User;
import app.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ProfileControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private ActivityLogService activityLogService;

    @MockitoBean
    private CloudinaryService cloudinaryService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testUser");
        testUser.setEmail("testUser@example.com");
        testUser.setPassword("password");
        testUser.setRole(Role.USER);
        testUser.setDateRegistered(LocalDateTime.now());
        testUser.setIsActive(true);
        testUser = userRepository.save(testUser);
    }

    @Test
    void testGetProfilePage_ShouldReturnProfilePage() throws Exception {
        UserDetails userDetails = new CustomUserDetails(testUser.getId(), testUser.getUsername(), testUser.getPassword(), testUser.getRole(), testUser.getIsActive());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication); // use this because Spring does not recognize my CustomUserDetails

        mockMvc.perform(get("/my-profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("userProfileInfo"));
    }

    @Test
    @WithMockUser(username = "testUser")
    void testChangeMyProfilePicture_ShouldSucceed() throws Exception {
        MockMultipartFile profilePicture = new MockMultipartFile(
                "profilePicture", "profile.jpg", "image/jpeg", "test image content".getBytes()
        );

        ImageUploadResult mockResult = new ImageUploadResult("https://mock-cloudinary-url.com/test.jpg", "test");

        when(cloudinaryService.uploadImage(any(MultipartFile.class)))
                .thenReturn(mockResult);

        mockMvc.perform(multipart(HttpMethod.PUT, "/my-profile/" + testUser.getId() + "/change-picture") // Use HttpMethod.PUT
                        .file(profilePicture)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-profile"))
                .andExpect(flash().attribute("success", "Profile picture updated successfully"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    void testChangeProfilePicture_FileEmpty_ShouldReturnError() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile("profilePicture", new byte[0]);

        mockMvc.perform(multipart(HttpMethod.PUT, "/my-profile/" + testUser.getId() + "/change-picture")
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

        mockMvc.perform(multipart(HttpMethod.PUT, "/my-profile/" + testUser.getId() + "/change-picture")
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

        mockMvc.perform(multipart(HttpMethod.PUT, "/my-profile/{id}/change-picture", testUser.getId())
                        .file(invalidFile)
                        .with(csrf()))
                .andExpect(status().isOk()) // Should return the profile page, not a redirect
                .andExpect(view().name("profile")) // Should stay on profile page
                .andExpect(model().attributeExists("openPictureModal")) // Modal should be reopened
                .andExpect(model().attributeHasFieldErrors("changeProfilePicture", "profilePicture")); // Error should exist
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    void testChangeUsername_ShouldChangeUsername() throws Exception {
        mockMvc.perform(put("/my-profile/" + testUser.getId() + "/change-username")
                        .param("username", "newUsername")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-profile"));

        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertEquals("newUsername", updatedUser.getUsername());
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    void testChangeUsername_UsernameExists_ShouldReturnError() throws Exception {
        mockMvc.perform(put("/my-profile/" + testUser.getId() + "/change-username")
                        .param("username", "testUser") //existing username, in this case the same
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("changeUsername"))
                .andExpect(model().attributeExists("openUsernameModal"))
                .andExpect(model().attribute("openUsernameModal", true));
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    void testChangeUsername_UsernameTooShort_ShouldReturnError() throws Exception {
        mockMvc.perform(put("/my-profile/{id}/change-username", testUser.getId())
                        .param("username", "abc") // Simulate form input
                        .with(csrf()))
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("changeUsername"))
                .andExpect(model().attributeExists("openUsernameModal"))
                .andExpect(model().attribute("openUsernameModal", true));
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    void testChangeEmail_ShouldSucceed() throws Exception {
        mockMvc.perform(put("/my-profile/" + testUser.getId() + "/change-email")
                        .param("email", "new@example.com")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-profile"));

        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertEquals("new@example.com", updatedUser.getEmail());
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    void testChangeEmail_EmailExists_ShouldReturnError() throws Exception {
        mockMvc.perform(put("/my-profile/" + testUser.getId() + "/change-email")
                        .param("email", "testUser@example.com")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("changeEmail"))
                .andExpect(model().attributeExists("openEmailModal"))
                .andExpect(model().attribute("openEmailModal", true));
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    void testChangeEmailEmpty() throws Exception {
        mockMvc.perform(put("/my-profile/{id}/change-email", testUser.getId())
                        .param("email", "")
                        .with(csrf()))
                .andExpect(status().isOk()) // Should stay on the profile page
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("changeEmail"))
                .andExpect(model().attributeExists("openEmailModal"))
                .andExpect(model().attribute("openEmailModal", true));
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    void testChangeEmailInvalidFormat() throws Exception {
        mockMvc.perform(put("/my-profile/{id}/change-email", testUser.getId())
                        .param("email", "invalid-email")
                        .with(csrf()))
                .andExpect(status().isOk()) // Should stay on the profile page
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("changeEmail"))
                .andExpect(model().attributeExists("openEmailModal"))
                .andExpect(model().attribute("openEmailModal", true));
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    void testChangePassword_ShouldSucceed() throws Exception {
        mockMvc.perform(put("/my-profile/" + testUser.getId() + "/change-password")
                        .param("password", "newPassword")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-profile"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    void testChangePassword_LessThan6Characters_ShouldReturnError() throws Exception {
        mockMvc.perform(put("/my-profile/" + testUser.getId() + "/change-password")
                        .param("password", "newPa")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("changePassword"))
                .andExpect(model().attributeExists("openPasswordModal"))
                .andExpect(model().attribute("openPasswordModal", true));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void testActivityLog_ShouldReturnActivityLogPage() throws Exception {
        ActivityLogResponse response1 = ActivityLogResponse.builder()
                .userId(testUser.getId())
                .action("Recipe added")
                .createdOn(LocalDateTime.now())
                .build();

        ActivityLogResponse response2 = ActivityLogResponse.builder()
                .userId(testUser.getId())
                .action("Comment added")
                .createdOn(LocalDateTime.now().plusDays(1))
                .build();

        List<ActivityLogResponse> mockLogs = List.of(response1, response2);

        Mockito.when(activityLogService.getActivityLog(testUser.getId())).thenReturn(mockLogs);

        mockMvc.perform(get("/my-profile/{id}/activity-log", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("activity-log"))
                .andExpect(model().attributeExists("activityLog"))
                .andExpect(model().attribute("activityLog", mockLogs))
                .andExpect(model().attributeExists("userId"))
                .andExpect(model().attribute("userId", testUser.getId()));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void testClearActivityLog_ShouldDeleteAllLoggedActivities() throws Exception {
        ActivityLogResponse response1 = ActivityLogResponse.builder()
                .userId(testUser.getId())
                .action("Recipe added")
                .createdOn(LocalDateTime.now())
                .build();

        ActivityLogResponse response2 = ActivityLogResponse.builder()
                .userId(testUser.getId())
                .action("Comment added")
                .createdOn(LocalDateTime.now().plusDays(1))
                .build();

        List<ActivityLogResponse> mockLogs = List.of(response1, response2);

        Mockito.when(activityLogService.getActivityLog(testUser.getId())).thenReturn(mockLogs);

        // Perform GET request before deletion to verify logs exist
        mockMvc.perform(get("/my-profile/{id}/activity-log", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("activity-log"))
                .andExpect(model().attributeExists("activityLog"))
                .andExpect(model().attribute("activityLog", mockLogs))
                .andExpect(model().attributeExists("userId"))
                .andExpect(model().attribute("userId", testUser.getId()));

        // Step 2: Mock delete behavior
        doNothing().when(activityLogService).deleteLogsByUserId(testUser.getId());

        // Perform DELETE request
        mockMvc.perform(delete("/my-profile/{id}/activity-log/clear", testUser.getId())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-profile/" + testUser.getId() + "/activity-log"));

        // Step 3: Mock the activity log after deletion (empty)
        Mockito.when(activityLogService.getActivityLog(testUser.getId())).thenReturn(Collections.emptyList());

        // Perform GET request after deletion to verify logs are empty
        mockMvc.perform(get("/my-profile/{id}/activity-log", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("activityLog"))
                .andExpect(model().attribute("activityLog", Collections.emptyList()));

        // Verify interactions
        verify(activityLogService, times(1)).deleteLogsByUserId(testUser.getId());
    }
}
