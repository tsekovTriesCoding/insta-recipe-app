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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static app.TestBuilder.aRandomRegisterRequest;
import static app.TestBuilder.aRandomUser;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserService userService;


    @Test
    void testRegister_shouldRegisterUser() {
        RegisterRequest registerRequest = aRandomRegisterRequest();
        User user = aRandomUser();
        String encodedPassword = "$2a$10$encodedPassword123";

        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.register(registerRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertNotNull(savedUser);
        assertEquals(registerRequest.getUsername(), savedUser.getUsername());
        assertEquals(registerRequest.getEmail(), savedUser.getEmail());
        assertEquals(encodedPassword, savedUser.getPassword());

        verify(passwordEncoder).encode(registerRequest.getPassword());
        verify(eventPublisher).publishEvent(any(ActivityLogEvent.class));
    }

    @Test
    void testRegister_ThrowsException_WhenUsernameExists() {
        RegisterRequest request = aRandomRegisterRequest();

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.register(request));
        verify(userRepository, never()).save(any(User.class));
        verify(eventPublisher, never()).publishEvent(any(ActivityLogEvent.class));
    }

    @Test
    void testRegister_shouldThrowException_whenEmailExists() {
        RegisterRequest request = aRandomRegisterRequest();

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.register(request));
        verify(userRepository, never()).save(any(User.class));
        verify(eventPublisher, never()).publishEvent(any(ActivityLogEvent.class));
    }

    @Test
    void testGetUserByUsername_shouldReturnsUser_whenUserExists() {
        String username = "testUser";
        User mockUser = new User();
        mockUser.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));

        User foundUser = userService.getUserByUsername(username);

        assertNotNull(foundUser);
        assertEquals(username, foundUser.getUsername());
    }

    @Test
    void testGetUserByUsername_shouldThrowException_whenUserNotFound() {
        String username = "nonExistentUser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () ->
                userService.getUserByUsername(username)
        );

        assertEquals("User with username " + username + " not found.", exception.getMessage());
    }

    @Test
    void testGetUserById_shouldReturnsUser_whenUserExists() {
        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        User foundUser = userService.getUserById(userId);

        assertNotNull(foundUser);
        assertEquals(userId, foundUser.getId());
    }

    @Test
    void testGetUserById_shouldThrowException_whenUserNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () ->
                userService.getUserById(userId)
        );

        assertEquals("User with id " + userId + " not found.", exception.getMessage());
    }

    @Test
    void testUpdateProfilePicture_shouldUpdateProfilePicture() {
        User user = aRandomUser();

        MockMultipartFile newImage = new MockMultipartFile("image", "test.jpg", "image/jpeg", new byte[1]);
        ImageUploadResult uploadResult = new ImageUploadResult("testUrl", "testId");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(cloudinaryService.uploadImage(any())).thenReturn(uploadResult);

        userService.updateProfilePicture(user.getId(), newImage);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertNotNull(savedUser);
        assertEquals(uploadResult.getImageUrl(), savedUser.getProfilePicture());
        assertEquals(uploadResult.getPublicId(), savedUser.getImagePublicId());

        verify(cloudinaryService, times(1)).uploadImage(any());
        verify(eventPublisher).publishEvent(any(ActivityLogEvent.class));
    }

    @Test
    void testUpdateProfilePicture_shouldFirstDeleteOldImage_thenUpdateProfilePicture() {
        User user = aRandomUser();
        user.setImagePublicId(UUID.randomUUID().toString());

        MockMultipartFile newImage = new MockMultipartFile("image", "test.jpg", "image/jpeg", new byte[1]);
        ImageUploadResult uploadResult = new ImageUploadResult("testUrl", "testId");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(cloudinaryService.uploadImage(any())).thenReturn(uploadResult);

        userService.updateProfilePicture(user.getId(), newImage);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertNotNull(savedUser);
        assertEquals(uploadResult.getImageUrl(), savedUser.getProfilePicture());
        assertEquals(uploadResult.getPublicId(), savedUser.getImagePublicId());

        verify(cloudinaryService, times(1)).deleteImage(any());
        verify(cloudinaryService, times(1)).uploadImage(any());
        verify(eventPublisher).publishEvent(any(ActivityLogEvent.class));
    }

    @Test
    public void testUpdateUsername_shouldUpdateUsername() {
        User user = aRandomUser();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        userService.updateUsername(user.getId(), "newUsername");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertNotNull(savedUser);
        assertEquals("newUsername", savedUser.getUsername());

        verify(eventPublisher).publishEvent(any(ActivityLogEvent.class));
    }

    @Test
    public void testUpdateEmail_shouldUpdateEmail() {
        User user = aRandomUser();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        userService.updateEmail(user.getId(), "newEmail");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertNotNull(savedUser);
        assertEquals("newEmail", savedUser.getEmail());

        verify(eventPublisher).publishEvent(any(ActivityLogEvent.class));
    }

    @Test
    public void testUpdatePassword_shouldUpdatePassword() {
        User user = aRandomUser();
        String encodedPassword = "$2a$10$encodedPassword123";

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(any())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.updatePassword(user.getId(), "newPassword");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertNotNull(savedUser);
        assertEquals(encodedPassword, savedUser.getPassword());

        verify(eventPublisher).publishEvent(any(ActivityLogEvent.class));
        verify(passwordEncoder, times(1)).encode(any());
    }

    @Test
    void testExistsByUsername_shouldReturnTrue_whenUserExists() {
        String username = "existingUser";
        when(userRepository.existsByUsername(username)).thenReturn(true);

        boolean result = userService.existsByUsername(username);

        assertTrue(result);
        verify(userRepository).existsByUsername(username);
    }

    @Test
    void testExistsByUsername_shouldReturnFalse_whenUserDoesNotExist() {
        String username = "nonExistentUser";
        when(userRepository.existsByUsername(username)).thenReturn(false);

        boolean result = userService.existsByUsername(username);

        assertFalse(result);
        verify(userRepository).existsByUsername(username);
    }

    @Test
    void testExistsByEmail_shouldReturnTrue_whenEmailExists() {
        String email = "user@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        boolean result = userService.existsByEmail(email);

        assertTrue(result);
        verify(userRepository).existsByEmail(email);
    }

    @Test
    void testExistsByEmail_shouldReturnFalse_whenEmailDoesNotExist() {
        String email = "nonexistent@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);

        boolean result = userService.existsByEmail(email);

        assertFalse(result);
        verify(userRepository).existsByEmail(email);
    }

    @Test
    void testChangeRole_shouldChangeRoleFromAdminToUser() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setRole(Role.ADMIN);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.changeUserRole(userId);

        assertEquals(Role.USER, user.getRole());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testChangeRole_shouldChangeRoleFromUserToAdmin() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setRole(Role.USER);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.changeUserRole(userId);

        assertEquals(Role.ADMIN, user.getRole());
        verify(userRepository).save(user);
    }

    @Test
    void testChangeUserRole_shouldThrowException_whenUserNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () ->
                userService.changeUserRole(userId)
        );

        assertEquals("User with id " + userId + " not found.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
}
