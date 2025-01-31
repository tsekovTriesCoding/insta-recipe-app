package app.web.dto;

import app.vallidation.annotation.UniqueEmail;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangeEmail {
    @NotBlank(message = "Email must not be empty")
    @Email(message = "Oops! The email address you entered seems to be invalid")
    @UniqueEmail
    private String email;
}
