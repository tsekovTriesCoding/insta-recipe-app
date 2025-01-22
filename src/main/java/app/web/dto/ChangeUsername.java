package app.web.dto;

import app.vallidation.annotation.UniqueUsername;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangeUsername {
    @NotBlank
    @UniqueUsername
    @Size(min = 5)
    private String username;
}
