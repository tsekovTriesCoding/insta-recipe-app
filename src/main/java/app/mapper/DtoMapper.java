package app.mapper;

import app.user.model.User;
import app.web.dto.UserProfileInfo;
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
}
