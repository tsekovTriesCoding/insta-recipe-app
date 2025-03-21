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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Rolls back changes after each test
public class UserServiceIT {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventCaptureConfig eventCaptureConfig; // Captures the events.The @EventListener inside EventCaptureConfig will catch events published by UserService in a real database-backed test

    @MockitoBean
    private CloudinaryService cloudinaryService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private RegisterRequest registerRequest;

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

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @BeforeEach
    public void setUp() {
        registerRequest = RegisterRequest.builder()
                .username("username")
                .email("email@example.com")
                .password("password")
                .build();
    }

    @Test
    public void testRegisterSuccess() {
        userService.register(registerRequest);

        Optional<User> savedUser = userRepository.findByUsername("username");
        assertTrue(savedUser.isPresent());
        assertEquals("username", savedUser.get().getUsername());
        assertEquals("email@example.com", savedUser.get().getEmail());
        assertTrue(passwordEncoder.matches(registerRequest.getPassword(), savedUser.get().getPassword()));
    }

    @Test
    public void testRegisterUsernameAlreadyExists() {
        User user = User.builder()
                .username("username")
                .email("email@example.com")
                .password("password")
                .role(Role.ADMIN)
                .dateRegistered(LocalDateTime.now())
                .build();

        userRepository.save(user);

        assertThrows(UserAlreadyExistsException.class, () -> userService.register(registerRequest));
    }

    @Test
    public void testRegisterEmailAlreadyExists() {
        User user = User.builder()
                .username("otherusername")
                .email("email@example.com")
                .password("password")
                .role(Role.ADMIN)
                .dateRegistered(LocalDateTime.now())
                .build();

        userRepository.save(user);

        assertThrows(UserAlreadyExistsException.class, () -> userService.register(registerRequest));
    }

    @Test
    public void testUpdateProfilePictureWhenUserHasDefaultPicture() {
        User user = User.builder()
                .username("otherusername")
                .email("email@example.com")
                .password("password")
                .role(Role.ADMIN)
                .dateRegistered(LocalDateTime.now())
                .build();
        userRepository.save(user);

        UUID userId = user.getId();

        MultipartFile newImage = mock(MultipartFile.class);
        ImageUploadResult mockUploadResult = new ImageUploadResult("new-image-url", "new-public-id");

        when(cloudinaryService.uploadImage(any(MultipartFile.class))).thenReturn(mockUploadResult);

        userService.updateProfilePicture(userId, newImage);

        List<ActivityLogEvent> capturedEvents = eventCaptureConfig.getCapturedEvents();
        ActivityLogEvent event = capturedEvents.get(0);
        String expectedMessage = "You have successfully updated your profile picture";

        User updatedUser = userRepository.findById(userId).orElseThrow();
        assertEquals("new-image-url", updatedUser.getProfilePicture());
        assertEquals("new-public-id", updatedUser.getImagePublicId());
        assertNotNull(updatedUser.getDateUpdated());

        assertFalse(capturedEvents.isEmpty(), "No events were captured!");
        assertEquals(expectedMessage, event.getAction());

        // Clean up captured events (important for avoiding test interference)
        eventCaptureConfig.clearCapturedEvents();

        verify(cloudinaryService, never()).deleteImage(anyString());
        verify(cloudinaryService, times(1)).uploadImage(any(MultipartFile.class));
    }

    @Test
    public void testUpdateProfilePictureWhenUserHasUpdatedPicture() {
        User user = User.builder()
                .username("otherusername")
                .email("email@example.com")
                .password("password")
                .role(Role.ADMIN)
                .profilePicture("old-image-url")
                .imagePublicId("old-public-id")
                .dateRegistered(LocalDateTime.now())
                .build();
        userRepository.save(user);

        UUID userId = user.getId();

        // Mock Cloudinary upload result
        MultipartFile newImage = mock(MultipartFile.class);
        ImageUploadResult mockUploadResult = new ImageUploadResult("new-image-url", "new-public-id");

        when(cloudinaryService.uploadImage(any(MultipartFile.class))).thenReturn(mockUploadResult);

        // Act: Call updateProfilePicture
        userService.updateProfilePicture(userId, newImage);

        List<ActivityLogEvent> capturedEvents = eventCaptureConfig.getCapturedEvents();
        ActivityLogEvent event = capturedEvents.get(0);
        String expectedMessage = "You have successfully updated your profile picture";

        // Assert: Fetch updated user from DB
        User updatedUser = userRepository.findById(userId).orElseThrow();
        assertEquals("new-image-url", updatedUser.getProfilePicture());
        assertEquals("new-public-id", updatedUser.getImagePublicId());
        assertNotNull(updatedUser.getDateUpdated());

        assertFalse(capturedEvents.isEmpty(), "No events were captured!");
        assertEquals(expectedMessage, event.getAction());

        // Clean up captured events (important for avoiding test interference)
        eventCaptureConfig.clearCapturedEvents();

        // Verify Cloudinary delete was called ONCE
        verify(cloudinaryService, times(1)).deleteImage("old-public-id");
        // Verify Cloudinary upload was called ONCE
        verify(cloudinaryService, times(1)).uploadImage(any(MultipartFile.class));
    }

    @Test
    public void testUpdateUsernameSuccess() {
        User user = User.builder()
                .username("otherusername")
                .email("email@example.com")
                .password("password")
                .role(Role.ADMIN)
                .dateRegistered(LocalDateTime.now())
                .build();
        String newUsername = "newUsername";

        User testUser = userRepository.save(user);
        UUID testUserId = testUser.getId();

        userService.updateUsername(testUserId, newUsername);

        List<ActivityLogEvent> capturedEvents = eventCaptureConfig.getCapturedEvents();
        ActivityLogEvent event = capturedEvents.get(0);
        String expectedMessage = "You have successfully updated your username to: " + newUsername;

        User updatedUser = userRepository.findById(testUserId).orElseThrow();
        assertNotNull(updatedUser);
        assertEquals(newUsername, updatedUser.getUsername());

        assertFalse(capturedEvents.isEmpty(), "No events were captured!");
        assertEquals(expectedMessage, event.getAction());

        // Clean up captured events (important for avoiding test interference)
        eventCaptureConfig.clearCapturedEvents();
    }

    @Test
    public void testUpdateEmailSuccess() {
        User user = User.builder()
                .username("otherusername")
                .email("email@example.com")
                .password("password")
                .role(Role.ADMIN)
                .dateRegistered(LocalDateTime.now())
                .build();
        String newEmail = "newemail@example.com";

        User testUser = userRepository.save(user);
        UUID testUserId = testUser.getId();

        userService.updateEmail(testUserId, newEmail);

        List<ActivityLogEvent> capturedEvents = eventCaptureConfig.getCapturedEvents();
        ActivityLogEvent event = capturedEvents.get(0);
        String expectedMessage = "You have successfully updated your email to: " + newEmail;

        User updatedUser = userRepository.findById(testUserId).orElseThrow();
        assertNotNull(updatedUser);
        assertEquals(newEmail, updatedUser.getEmail());

        assertFalse(capturedEvents.isEmpty(), "No events were captured!");
        assertEquals(expectedMessage, event.getAction());

        // Clean up captured events (important for avoiding test interference)
        eventCaptureConfig.clearCapturedEvents();
    }

    @Test
    public void testUpdatePasswordSuccess() {
        User user = User.builder()
                .username("otherusername")
                .email("email@example.com")
                .password(passwordEncoder.encode("oldPassword"))
                .role(Role.ADMIN)
                .dateRegistered(LocalDateTime.now())
                .build();
        String newPassword = "newSecurePassword";

        User testUser = userRepository.save(user);
        UUID testUserId = testUser.getId();

        userService.updatePassword(testUserId, newPassword);

        List<ActivityLogEvent> capturedEvents = eventCaptureConfig.getCapturedEvents();
        ActivityLogEvent event = capturedEvents.get(0);
        String expectedMessage = "You have successfully updated your password";

        User updatedUser = userRepository.findById(testUserId).orElseThrow();
        assertNotNull(updatedUser);
        assertTrue(passwordEncoder.matches(newPassword, updatedUser.getPassword()));

        assertFalse(capturedEvents.isEmpty(), "No events were captured!");
        assertEquals(expectedMessage, event.getAction());

        // Clean up captured events (important for avoiding test interference)
        eventCaptureConfig.clearCapturedEvents();
    }

    @Test
    public void testGetAllShouldReturnAllUsers() {
        User user1 = User.builder()
                .username("user1")
                .email("email@example1.com")
                .password("password")
                .role(Role.ADMIN)
                .dateRegistered(LocalDateTime.now())
                .isActive(true)
                .build();

        userRepository.save(user1);

        User user2 = User.builder()
                .username("user2")
                .email("email@example2.com")
                .password("password")
                .role(Role.USER)
                .dateRegistered(LocalDateTime.now())
                .isActive(true)
                .build();

        userRepository.save(user2);

        List<User> users = userService.getAll();

        assertNotNull(users);
        assertEquals(2, users.size());

        assertTrue(users.stream().anyMatch(u -> u.getUsername().equals("user1") && u.getRole().equals(Role.ADMIN)));
        assertTrue(users.stream().anyMatch(u -> u.getUsername().equals("user2") && u.getRole().equals(Role.USER)));
    }

    @Test
    public void testGetAllShouldReturnEmptyListWhenNoUsers() {
        List<User> users = userService.getAll();

        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    public void testChangeUserRoleShouldToggleRoleFroAdminToUser() {
        User user = User.builder()
                .username("otherusername")
                .email("email@example.com")
                .password(passwordEncoder.encode("oldPassword"))
                .role(Role.ADMIN)
                .dateRegistered(LocalDateTime.now())
                .build();

        userRepository.save(user);

        userService.changeUserRole(user.getId());

        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertNotNull(updatedUser);
        assertEquals(Role.USER, updatedUser.getRole());
    }

    @Test
    public void testChangeUserRoleShouldToggleRoleFromUserToAdmin() {
        User user = User.builder()
                .username("otherusername")
                .email("email@example.com")
                .password(passwordEncoder.encode("oldPassword"))
                .role(Role.USER)
                .dateRegistered(LocalDateTime.now())
                .build();

        userRepository.save(user);

        userService.changeUserRole(user.getId());

        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertNotNull(updatedUser);
        assertEquals(Role.ADMIN, updatedUser.getRole());
    }

    @Test
    public void testChangeUserRoleShouldThrowExceptionWhenUserNotFound() {
        UUID nonExistentUserId = UUID.randomUUID();

        assertThrows(UserNotFoundException.class, () -> {
            userService.changeUserRole(nonExistentUserId);
        });
    }
}