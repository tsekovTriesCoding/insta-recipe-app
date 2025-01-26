package app.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddComment {
    @NotBlank
    private String content;
}
