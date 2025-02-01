package app.web.dto;

import app.vallidation.annotation.UniqueEmail;
import app.vallidation.annotation.UniqueUsername;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotNull
    @Size(min = 5, message = "Username must be at least 5 symbols")
    @UniqueUsername
    private String username;

    @NotBlank(message = "Email must not be empty")
    @Email(message = "Oops! The email address you entered seems to be invalid")
    @UniqueEmail
    private String email;

    @NotNull
    @Size(min = 6, message = "Password must be at least 6 symbols")
    private String password;

    @NotNull
    @Size(min = 6, message = "Password must be at least 6 symbols")
    private String confirmPassword;
}
