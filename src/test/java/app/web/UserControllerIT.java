package app.web;

import app.user.model.Role;
import app.user.model.User;
import app.user.repository.UserRepository;
import app.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testGetLoginPage() throws Exception {
        mockMvc.perform(get("/users/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeDoesNotExist("error"));
    }

    @Test
    void testGetLoginPageWithError() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("error", "Invalid username or password.");

        mockMvc.perform(get("/users/login").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("error", "Invalid username or password."));
    }

    @Test
    void testGetRegisterPage() throws Exception {
        mockMvc.perform(get("/users/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registerRequest"));
    }

    @Test
    void testRegisterWithValidData() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("testUser")
                .email("testEmail@abv.bg")
                .password("testPassword")
                .confirmPassword("testPassword")
                .build();

        mockMvc.perform(post("/users/register")
                        .with(csrf())
                        .flashAttr("registerRequest", registerRequest))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    void testRegisterWithInvalidData() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("user")
                .email("testEmail")
                .password("test")
                .confirmPassword("test")
                .build();

        mockMvc.perform(post("/users/register")
                        .with(csrf())
                        .flashAttr("registerRequest", registerRequest))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("registerRequest", "username", "email", "password", "confirmPassword"));
    }

    @Test
    void testRegisterWithExistingUsername() throws Exception {
        User user = new User();
        user.setUsername("testUser");
        user.setEmail("testUser@example.com");
        user.setPassword("password");
        user.setRole(Role.USER);
        user.setDateRegistered(LocalDateTime.now());
        user.setIsActive(true);
        userRepository.save(user);

        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("testUser")
                .email("newEmail@abv.bg")
                .password("testPassword")
                .confirmPassword("testPassword")
                .build();

        mockMvc.perform(post("/users/register")
                        .with(csrf())
                        .flashAttr("registerRequest", registerRequest))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("registerRequest","username"));
    }

    @Test
    void testRegisterWithExistingEmail() throws Exception {
        User user = new User();
        user.setUsername("testUser");
        user.setEmail("testUser@example.com");
        user.setPassword("password");
        user.setRole(Role.USER);
        user.setDateRegistered(LocalDateTime.now());
        user.setIsActive(true);
        userRepository.save(user);

        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("newUser")
                .email("testUser@example.com")
                .password("testPassword")
                .confirmPassword("testPassword")
                .build();

        mockMvc.perform(post("/users/register")
                        .with(csrf())
                        .flashAttr("registerRequest", registerRequest))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("registerRequest","email"));
    }
}
