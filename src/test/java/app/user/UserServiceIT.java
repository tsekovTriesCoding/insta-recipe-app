package app.user;

import app.cloudinary.CloudinaryService;
import app.exception.UserAlreadyExistsException;
import app.exception.UserNotFoundException;
import app.user.model.Role;
import app.user.model.User;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.web.dto.RegisterRequest;
import app.web.dto.UserWithRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Rolls back changes after each test
public class UserServiceIT {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private CloudinaryService cloudinaryService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private RegisterRequest registerRequest;

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
    public void testUpdateProfilePictureSuccess() {
        User user = User.builder()
                .username("otherusername")
                .email("email@example.com")
                .password("password")
                .role(Role.ADMIN)
                .dateRegistered(LocalDateTime.now())
                .build();
        userRepository.save(user);

        MockMultipartFile file = new MockMultipartFile("file", "profile.jpg", "image/jpeg", "some image content".getBytes());

        String imageUrl = "http://cloudinary.com/abcd1234";
        Mockito.when(cloudinaryService.uploadImage(file)).thenReturn(imageUrl);

        UUID userId = userRepository.findAll().get(0).getId();
        userService.updateProfilePicture(userId, file);

        User updatedUser = userRepository.findById(userId).orElseThrow();
        assertNotNull(updatedUser.getProfilePicture());
        assertEquals(imageUrl, updatedUser.getProfilePicture());
        assertNotNull(updatedUser.getDateUpdated());
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

        User updatedUser = userRepository.findById(testUserId).orElseThrow();
        assertNotNull(updatedUser);
        assertEquals(newUsername, updatedUser.getUsername());
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

        User updatedUser = userRepository.findById(testUserId).orElseThrow();
        assertNotNull(updatedUser);
        assertEquals(newEmail, updatedUser.getEmail());
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


        User updatedUser = userRepository.findById(testUserId).orElseThrow();
        assertNotNull(updatedUser);
        assertTrue(passwordEncoder.matches(newPassword, updatedUser.getPassword()));
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

        List<UserWithRole> users = userService.getAll();

        assertNotNull(users);
        assertEquals(2, users.size());

        assertTrue(users.stream().anyMatch(u -> u.getUsername().equals("user1") && u.getRole().equals(Role.ADMIN)));
        assertTrue(users.stream().anyMatch(u -> u.getUsername().equals("user2") && u.getRole().equals(Role.USER)));
    }

    @Test
    public void testGetAllShouldReturnEmptyListWhenNoUsers() {
        List<UserWithRole> users = userService.getAll();

        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    public void testUpdateUserRoleShouldToggleRoleFroAdminToUser() {
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
    public void testUpdateUserRoleShouldToggleRoleFromUserToAdmin() {
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
    public void testUpdateUserRoleShouldThrowExceptionWhenUserNotFound() {
        UUID nonExistentUserId = UUID.randomUUID();

        assertThrows(UserNotFoundException.class, () -> {
            userService.changeUserRole(nonExistentUserId);
        });
    }
}