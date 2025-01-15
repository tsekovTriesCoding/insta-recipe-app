package app.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @Size(min = 6, message = "Username must be at least 6 symbols")
    private String username;

    @Email
    private String email;

    @Size(min = 6, message = "Password must be at least 6 symbols")
    private String password;

    @Size(min = 6, message = "Password must be at least 6 symbols")
    private String confirmPassword;
}
