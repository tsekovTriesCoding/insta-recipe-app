package app.web.dto;

import app.user.model.Role;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Builder
@Data
public class UserWithRole {
    private UUID id;
    private String username;
    private String email;
    private Role role;
    private boolean isActive;
}
