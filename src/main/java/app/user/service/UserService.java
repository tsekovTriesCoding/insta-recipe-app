package app.user.service;

import app.exception.DomainException;
import app.user.model.Role;
import app.user.model.User;
import app.user.repository.UserRepository;
import app.web.dto.RegisterRequest;
import app.web.dto.UserProfileInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(RegisterRequest registerRequest) {

        Optional<User> optionalUser = userRepository.findByUsername((registerRequest.getUsername()));

        if (optionalUser.isPresent()) {
            throw new DomainException("Username [%s] already exist.".formatted(registerRequest.getUsername()));
        }

        User user = userRepository.save(initializeUser(registerRequest));

        log.info("Successfully create new user account for username [%s] and id [%s]".formatted(user.getUsername(), user.getId()));

        return user;
    }

    public UserProfileInfo getByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::map)
                .orElseThrow(NoSuchElementException::new);
    }

    public UserProfileInfo update(UserProfileInfo userProfileInfo, MultipartFile file, String username) throws IOException {
        Path destinationFile = Paths
                .get("src", "main", "resources", "static/images/uploads", userProfileInfo.getUsername() + "-profile-picture.png")
                .normalize()
                .toAbsolutePath();

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        }

        User user = userRepository.findByUsername(username).orElseThrow(NoSuchElementException::new);

        verifyNewInfo(userProfileInfo, user, destinationFile);
        user.setDateUpdated(LocalDateTime.now());

        User updated = userRepository.save(user);

        return map(updated);
    }

    private void verifyNewInfo(UserProfileInfo userProfileInfo, User user, Path destinationFile) {
        if (userProfileInfo.getUsername() != null && !userProfileInfo.getUsername().isEmpty()) {
            user.setUsername(userProfileInfo.getUsername());
        }

        if (userProfileInfo.getEmail() != null && !userProfileInfo.getEmail().isEmpty()) {
            user.setEmail(userProfileInfo.getEmail());
        }

        if (userProfileInfo.getPassword() != null && !userProfileInfo.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userProfileInfo.getPassword()));
        }

        if (destinationFile.toFile().exists()) {
            user.setProfilePicture("/images/uploads/" + destinationFile.getFileName().toString());
        }
    }

    private User initializeUser(RegisterRequest registerRequest) {
        return User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .profilePicture("/images/default-profile.png")
                .dateRegistered(LocalDateTime.now())
                .role(Role.USER)
                .build();
    }

    private UserProfileInfo map(User user) {
        UserProfileInfo userProfileInfo = new UserProfileInfo();
        userProfileInfo.setUsername(user.getUsername());
        userProfileInfo.setEmail(user.getEmail());
        userProfileInfo.setPassword(user.getPassword());
        userProfileInfo.setProfilePictureUrl(user.getProfilePicture());

        return userProfileInfo;
    }
}
