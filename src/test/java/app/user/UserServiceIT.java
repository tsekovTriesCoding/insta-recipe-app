package app.user;

import app.activitylog.event.ActivityLogEvent;
import app.cloudinary.dto.ImageUploadResult;
import app.cloudinary.service.CloudinaryService;
import app.exception.UserAlreadyExistsException;
import app.exception.UserNotFoundException;
import app.user.model.Role;
import app.user.model.User;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.web.dto.RegisterRequest;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static app.TestBuilder.aRandomRegisterRequest;
import static app.TestBuilder.aRandomWithoutId;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class UserServiceIT {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private CloudinaryService cloudinaryService;

    @Autowired
    private EventCaptureConfig eventCaptureConfig; // Captures the events.The @EventListener inside EventCaptureConfig will catch events published by UserService in a real database-backed test

    private static WireMockServer wireMockServer;

    // Do not use @MockBean ApplicationEventPublisher in integration tests.
    //It replaces the real publisher with a mock, preventing real event propagation.
    //Mocking ApplicationEventPublisher in integration tests can bypass the event-driven nature of Spring, which isn't ideal for testing how your application reacts to events.
    @TestConfiguration
    static class EventCaptureConfig {

        private final List<ActivityLogEvent> capturedEvents = new ArrayList<>();

        @EventListener
        public void onActivityLogEvent(ActivityLogEvent event) {
            capturedEvents.add(event);
        }

        public void clearCapturedEvents() {
            capturedEvents.clear();
        }

        public List<ActivityLogEvent> getCapturedEvents() {
            return capturedEvents;
        }
    }

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(8081); // Same port as the real service
        wireMockServer.start();

        wireMockServer.stubFor(post(urlEqualTo("/api/v1/activity-log"))
                .willReturn(aResponse().withStatus(200))); // Mock successful response
    }

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @Test
    void testRegister_shouldRegisterUser() {
        RegisterRequest registerRequest = aRandomRegisterRequest();

        userService.register(registerRequest);

        List<ActivityLogEvent> capturedEvents = eventCaptureConfig.getCapturedEvents();
        ActivityLogEvent event = capturedEvents.get(0);
        String expectedMessage = "You have successfully registered with username: " + registerRequest.getUsername();

        Optional<User> user = userRepository.findByUsername(registerRequest.getUsername());
        assertTrue(user.isPresent());
        assertEquals(registerRequest.getUsername(), user.get().getUsername());
        assertEquals(registerRequest.getEmail(), user.get().getEmail());
        assertTrue(passwordEncoder.matches(registerRequest.getPassword(), user.get().getPassword()));

        assertFalse(capturedEvents.isEmpty(), "No events were captured!");
        assertEquals(expectedMessage, event.getAction());

        eventCaptureConfig.clearCapturedEvents();
    }

    @Test
    void testRegister_shouldThrowUserAlreadyExistsException_whenUsernameAlreadyExists() {
        User user = aRandomWithoutId();

        userRepository.save(user);

        RegisterRequest request = aRandomRegisterRequest();

        assertThrows(UserAlreadyExistsException.class, () -> userService.register(request));
    }

    @Test
    void testRegister_shouldThrowUserAlreadyExistsException_whenEmailAlreadyExists() {
        User user = aRandomWithoutId();


        userRepository.save(user);

        RegisterRequest request = aRandomRegisterRequest();
        request.setUsername("otherName");

        assertThrows(UserAlreadyExistsException.class, () -> userService.register(request));
    }

    @Test
    void testUpdateProfilePicture_shouldUpdateProfilePicture() {
        User user = aRandomWithoutId();
        userRepository.save(user);

        Optional<User> optionalUser = userRepository.findByUsername(user.getUsername());
        assertTrue(optionalUser.isPresent());

        User savedUser = optionalUser.get();

        MultipartFile newImage = mock(MultipartFile.class);
        ImageUploadResult uploadResult = new ImageUploadResult("newPublicId", "http://newimageurl.com");

        // Mock Cloudinary service's uploadImage method
        when(cloudinaryService.uploadImage(newImage)).thenReturn(uploadResult);

        // Act: Call the method that updates the profile picture
        userService.updateProfilePicture(savedUser.getId(), newImage);

        List<ActivityLogEvent> capturedEvents = eventCaptureConfig.getCapturedEvents();
        ActivityLogEvent event = capturedEvents.get(0);
        String expectedMessage = "You have successfully updated your profile picture";

        // Assert: Verify that the CloudinaryService's uploadImage was called
        verify(cloudinaryService, times(1)).uploadImage(newImage);

        User updatedUser = userRepository.findByUsername(savedUser.getUsername()).get();

        // Assert: Verify the user's profile picture and public ID were updated
        assertEquals(uploadResult.getImageUrl(), updatedUser.getProfilePicture());
        assertEquals(uploadResult.getPublicId(), updatedUser.getImagePublicId());

        assertFalse(capturedEvents.isEmpty(), "No events were captured!");
        assertEquals(expectedMessage, event.getAction());

        // Clean up captured events (important for avoiding test interference)
        eventCaptureConfig.clearCapturedEvents();

        // Verify Cloudinary delete was called ONCE
        verify(cloudinaryService, never()).deleteImage(anyString());
        // Verify Cloudinary upload was called ONCE
        verify(cloudinaryService, times(1)).uploadImage(any(MultipartFile.class));
    }

    @Test
    void testUpdateProfilePicture_shouldDeleteOldPicture_AndUpdateProfilePicture() {
        User user = aRandomWithoutId();

        // Arrange: Set up the user with an existing profile picture
        user.setImagePublicId("oldPublicId");
        user.setProfilePicture("http://oldimageurl.com");

        userRepository.save(user);

        // Mock the CloudinaryService to simulate deleting the old image
        doNothing().when(cloudinaryService).deleteImage("oldPublicId");

        // Mock the uploadImage method to simulate uploading the new image
        MultipartFile newImage = mock(MultipartFile.class);
        ImageUploadResult uploadResult = new ImageUploadResult("newPublicId", "http://newimageurl.com");
        when(cloudinaryService.uploadImage(newImage)).thenReturn(uploadResult);

        Optional<User> optionalUser = userRepository.findByUsername(user.getUsername());
        assertTrue(optionalUser.isPresent());

        User savedUser = optionalUser.get();

        // Act: Call the updateProfilePicture method
        userService.updateProfilePicture(savedUser.getId(), newImage);

        List<ActivityLogEvent> capturedEvents = eventCaptureConfig.getCapturedEvents();
        ActivityLogEvent event = capturedEvents.get(0);
        String expectedMessage = "You have successfully updated your profile picture";


        User updatedUser = userRepository.findByUsername(savedUser.getUsername()).get();

        // Assert: Verify that the user profile picture and public ID were updated
        assertEquals(uploadResult.getImageUrl(), updatedUser.getProfilePicture());
        assertEquals(uploadResult.getPublicId(), updatedUser.getImagePublicId());
        assertFalse(capturedEvents.isEmpty(), "No events were captured!");
        assertEquals(expectedMessage, event.getAction());

        // Clean up captured events (important for avoiding test interference)
        eventCaptureConfig.clearCapturedEvents();

        // Verify Cloudinary delete was called ONCE
        verify(cloudinaryService, times(1)).deleteImage("oldPublicId");
        // Verify Cloudinary upload was called ONCE
        verify(cloudinaryService, times(1)).uploadImage(any(MultipartFile.class));

    }

    @Test
    void testUpdateUsername_shouldUpdateUsername() {
        User user = aRandomWithoutId();
        userRepository.save(user);

        Optional<User> optionalUser = userRepository.findByUsername(user.getUsername());
        assertTrue(optionalUser.isPresent());

        User savedUser = optionalUser.get();

        userService.updateUsername(savedUser.getId(), "newUsername");

        List<ActivityLogEvent> capturedEvents = eventCaptureConfig.getCapturedEvents();
        ActivityLogEvent event = capturedEvents.get(0);
        String expectedMessage = "You have successfully updated your username to: " + "newUsername";

        Optional<User> updatedUser = userRepository.findByUsername("newUsername");
        assertTrue(updatedUser.isPresent());

        assertFalse(capturedEvents.isEmpty(), "No events were captured!");
        assertEquals(expectedMessage, event.getAction());

        // Clean up captured events (important for avoiding test interference)
        eventCaptureConfig.clearCapturedEvents();
    }

    @Test
    void testUpdateUsername_shouldThrowException_whenUserDoesNotExist() {
        UUID uuid = UUID.randomUUID();
        assertThrows(UserNotFoundException.class, () -> userService.updateUsername(uuid, "username"));
    }

    @Test
    void testUpdateEmail_shouldUpdateEmail() {
        User user = aRandomWithoutId();
        userRepository.save(user);

        Optional<User> optionalUser = userRepository.findByUsername(user.getUsername());
        assertTrue(optionalUser.isPresent());

        User savedUser = optionalUser.get();

        userService.updateEmail(savedUser.getId(), "newEmail");

        List<ActivityLogEvent> capturedEvents = eventCaptureConfig.getCapturedEvents();
        ActivityLogEvent event = capturedEvents.get(0);
        String expectedMessage = "You have successfully updated your email to: " + "newEmail";

        Optional<User> updatedUser = userRepository.findByEmail("newEmail");
        assertTrue(updatedUser.isPresent());

        assertFalse(capturedEvents.isEmpty(), "No events were captured!");
        assertEquals(expectedMessage, event.getAction());

        // Clean up captured events (important for avoiding test interference)
        eventCaptureConfig.clearCapturedEvents();
    }

    @Test
    void testUpdatePassword_shouldUpdatePassword() {
        User user = aRandomWithoutId();
        userRepository.save(user);

        Optional<User> optionalUser = userRepository.findByUsername(user.getUsername());
        assertTrue(optionalUser.isPresent());

        User savedUser = optionalUser.get();

        userService.updatePassword(savedUser.getId(), "newPassword");

        List<ActivityLogEvent> capturedEvents = eventCaptureConfig.getCapturedEvents();
        ActivityLogEvent event = capturedEvents.get(0);
        String expectedMessage = "You have successfully updated your password";

        Optional<User> optUpdatedUser = userRepository.findByUsername(savedUser.getUsername());
        assertTrue(optUpdatedUser.isPresent());

        User updatedUser = optUpdatedUser.get();

        assertTrue(passwordEncoder.matches("newPassword", updatedUser.getPassword()));

        assertFalse(capturedEvents.isEmpty(), "No events were captured!");
        assertEquals(expectedMessage, event.getAction());

        // Clean up captured events (important for avoiding test interference)
        eventCaptureConfig.clearCapturedEvents();
    }

    @Test
    void testChangeUserRole_ShouldChangeRoleFromUserToAdmin() {
        User user = aRandomWithoutId();
        userRepository.save(user);

        Optional<User> optionalUser = userRepository.findByUsername(user.getUsername());
        assertTrue(optionalUser.isPresent());
        User savedUser = optionalUser.get();

        userService.changeUserRole(savedUser.getId());

        Optional<User> optUpdatedUser = userRepository.findByUsername(savedUser.getUsername());
        assertTrue(optUpdatedUser.isPresent());
        User updatedUser = optUpdatedUser.get();

        assertEquals(Role.ADMIN, updatedUser.getRole());
    }

    @Test
    void testChangeUserRole_ShouldChangeRoleFromAdminToUser() {
        User user = aRandomWithoutId();
        user.setRole(Role.ADMIN);
        userRepository.save(user);

        Optional<User> optionalUser = userRepository.findByUsername(user.getUsername());
        assertTrue(optionalUser.isPresent());
        User savedUser = optionalUser.get();

        userService.changeUserRole(savedUser.getId());

        Optional<User> optUpdatedUser = userRepository.findByUsername(savedUser.getUsername());
        assertTrue(optUpdatedUser.isPresent());
        User updatedUser = optUpdatedUser.get();

        assertEquals(Role.USER, updatedUser.getRole());
    }

    @Test
    void testChangeUserStatus_ShouldChangeStatusFromActiveToInactive() {
        User user = aRandomWithoutId();
        userRepository.save(user);

        Optional<User> optionalUser = userRepository.findByUsername(user.getUsername());
        assertTrue(optionalUser.isPresent());
        User savedUser = optionalUser.get();

        userService.changeUserStatus(savedUser.getId());

        Optional<User> optUpdatedUser = userRepository.findByUsername(savedUser.getUsername());
        assertTrue(optUpdatedUser.isPresent());
        User updatedUser = optUpdatedUser.get();

        assertFalse(updatedUser.getIsActive());
    }

    @Test
    void testUpdateLastLogin_shouldUpdateLastLogin() {
        User user = aRandomWithoutId();
        user.setLastLogin(LocalDateTime.now().minusDays(5));
        userRepository.save(user);

        Optional<User> optionalUser = userRepository.findByUsername(user.getUsername());
        assertTrue(optionalUser.isPresent());
        User savedUser = optionalUser.get();

        userService.updateLastLogin(savedUser.getUsername());

        Optional<User> optUpdatedUser = userRepository.findByUsername(savedUser.getUsername());
        assertTrue(optUpdatedUser.isPresent());
        User updatedUser = optUpdatedUser.get();

        assertNotEquals(savedUser.getLastLogin(), updatedUser.getLastLogin());
    }
}
