package app.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangeUsername {
    @NotNull
    @Size(min = 5, message = "Username must be at least 5 symbols")
    private String username;
}
