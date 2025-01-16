package app.web.dto;

import lombok.Data;

@Data
public class UserProfileInfo {
    private String username;
    private String email;
    private String profilePictureUrl;
    private String password;
}
