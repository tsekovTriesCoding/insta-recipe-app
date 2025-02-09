package app.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UserProfileInfo {
    private UUID id;
    private String username;
    private String email;
    private String profilePictureUrl;
    private LocalDateTime dateRegistered;
    private LocalDateTime dateUpdated;
    private Boolean isActive;
}
