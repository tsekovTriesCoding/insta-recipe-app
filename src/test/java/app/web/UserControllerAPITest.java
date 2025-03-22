package app.web;

import app.exception.UserAlreadyExistsException;
import app.user.service.UserService;
import app.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@WithMockUser
class UserControllerAPITest {

    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

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
                        .formField("username", registerRequest.getUsername())
                        .formField("email", registerRequest.getEmail())
                        .formField("password", registerRequest.getPassword())
                        .formField("confirmPassword", registerRequest.getConfirmPassword()))
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
                        .formField("username", registerRequest.getUsername())
                        .formField("email", registerRequest.getEmail())
                        .formField("password", registerRequest.getPassword())
                        .formField("confirmPassword", registerRequest.getConfirmPassword()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("registerRequest", "username", "email", "password", "confirmPassword"));
    }

    @Test
    void testRegisterWithInvalidInputs() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("")
                .email("newEmail")
                .password("test")
                .confirmPassword("test")
                .build();

        mockMvc.perform(post("/users/register")
                        .with(csrf())
                        .formField("username", registerRequest.getUsername())
                        .formField("email", registerRequest.getEmail())
                        .formField("password", registerRequest.getPassword())
                        .formField("confirmPassword", registerRequest.getConfirmPassword()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("registerRequest", "username", "email", "password", "confirmPassword"));
    }

    @Test
    void testRegisterWithExistingUsername() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("testUser")
                .email("newEmail@abv.bg")
                .password("testPassword")
                .confirmPassword("testPassword")
                .build();

        doThrow(new UserAlreadyExistsException("User with username " + registerRequest.getUsername() + " already exists."))
                .when(userService).register(any(RegisterRequest.class));

        mockMvc.perform(post("/users/register")
                        .with(csrf())
                        .formField("username", registerRequest.getUsername())
                        .formField("email", registerRequest.getEmail())
                        .formField("password", registerRequest.getPassword())
                        .formField("confirmPassword", registerRequest.getConfirmPassword()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/users/register"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void testRegisterWithExistingEmail() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("newUser")
                .email("testUser@example.com")
                .password("testPassword")
                .confirmPassword("testPassword")
                .build();

        doThrow(new UserAlreadyExistsException("User with email " + registerRequest.getEmail() + " already exists."))
                .when(userService).register(any(RegisterRequest.class));

        mockMvc.perform(post("/users/register")
                        .with(csrf())
                        .formField("username", registerRequest.getUsername())
                        .formField("email", registerRequest.getEmail())
                        .formField("password", registerRequest.getPassword())
                        .formField("confirmPassword", registerRequest.getConfirmPassword()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/users/register"))
                .andExpect(flash().attributeExists("error"));
    }
}
