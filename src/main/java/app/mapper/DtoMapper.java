package app.mapper;

import app.user.model.User;
import app.web.dto.UserProfileInfo;
import app.web.dto.UserWithRole;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DtoMapper {

    public static UserProfileInfo mapUserToUserProfileInfo(User user) {
        return UserProfileInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .profilePictureUrl(user.getProfilePicture())
                .dateRegistered(user.getDateRegistered())
                .build();
    }

    public static UserWithRole mapUserToUserWithRole(User user) {
        return UserWithRole.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
