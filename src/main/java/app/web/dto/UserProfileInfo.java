package app.web.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
public class UserProfileInfo {
    private UUID id;
    private String username;
    private String email;
    private String profilePictureUrl;
    private LocalDateTime dateRegistered;
}
