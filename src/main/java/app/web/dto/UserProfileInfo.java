package app.web.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserProfileInfo {
    private UUID id;
    private String username;
    private String email;
    private String profilePictureUrl;
    private LocalDateTime dateRegistered;
}
