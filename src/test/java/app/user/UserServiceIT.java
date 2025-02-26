package app.user;

import app.cloudinary.CloudinaryService;
import app.exception.UserAlreadyExistsException;
import app.user.model.Role;
import app.user.model.User;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.web.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
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

        userRepository.deleteAll();
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
    public void testUpdateProfilePicture_Success() {
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
}