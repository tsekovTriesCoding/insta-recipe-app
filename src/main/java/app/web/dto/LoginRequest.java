package app.web.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @Size(min = 5, message = "Username must be at least 5 symbols")
    private String username;

    @Size(min = 6, message = "Password must be at least 6 symbols")
    private String password;
}
