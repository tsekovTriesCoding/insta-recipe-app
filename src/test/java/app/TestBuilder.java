package app;

import app.user.model.Role;
import app.user.model.User;
import app.web.dto.RegisterRequest;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.util.UUID;

@UtilityClass
public class TestBuilder {

    public static User aRandomUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .username("username")
                .email("email")
                .password("password")
                .profilePicture("/images/default-profile.png")
                .dateRegistered(LocalDateTime.now())
                .role(Role.USER)
                .isActive(true)
                .build();
    }

    public static RegisterRequest aRandomRegisterRequest() {
        return RegisterRequest.builder()
                .username("username")
                .email("email")
                .password("password")
                .confirmPassword("password")
                .build();

    }
}
