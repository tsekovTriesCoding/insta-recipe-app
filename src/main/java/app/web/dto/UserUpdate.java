package app.web.dto;

import lombok.Data;

@Data
public class UserUpdate {
    private String username;
    private String email;
    private String profilePictureUrl;
    private String password;
}
