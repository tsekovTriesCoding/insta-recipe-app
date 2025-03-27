package app.web.dto;

import app.vallidation.annotation.UniqueEmail;
import app.vallidation.annotation.UniqueUsername;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class RegisterRequest {

    @NotNull
    @Size(min = 5, max = 30, message = "Username must be between 5 and 30 symbols")
    @UniqueUsername
    private String username;

    @NotBlank(message = "Email must not be empty")
    @Email(message = "Oops! The email address you entered seems to be invalid")
    @UniqueEmail
    private String email;

    @NotNull
    @Size(min = 6, max = 30, message = "Password must be at between 6 and 50 symbols")
    private String password;

    @NotNull
    @Size(min = 6, max = 30, message = "Password must be at between 6 and 50 symbols")
    private String confirmPassword;
}
