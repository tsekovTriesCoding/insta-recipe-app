package app.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePassword {
    @NotNull
    @Size(min = 6, message = "Password must be at least 6 symbols")
    private String password;
}
