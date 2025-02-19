package app.user.service;

import app.cloudinary.CloudinaryService;
import app.exception.UserAlreadyExistsException;
import app.exception.UserNotFoundException;
import app.mapper.DtoMapper;
import app.security.CustomUserDetails;
import app.user.model.Role;
import app.user.model.User;
import app.user.repository.UserRepository;
import app.web.dto.RegisterRequest;
import app.web.dto.UserProfileInfo;
import app.web.dto.UserWithRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static app.mapper.DtoMapper.mapUserToUserProfileInfo;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    public User register(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new UserAlreadyExistsException("User with username " + registerRequest.getUsername() + " already exists.");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + registerRequest.getEmail() + " already exists.");
        }

        User user = userRepository.save(initializeUser(registerRequest));

        log.info("Successfully create new user account for username [%s] and email [%s], with id [%s]"
                .formatted(user.getUsername(), user.getEmail(), user.getId()));

        return user;
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User with username " + username + " not found."));
    }

    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id " + userId + " not found."));
    }

    public void updateProfilePicture(UUID userId,
                                     MultipartFile file) {

        String imageUrl = cloudinaryService.uploadImage(file);

        User user = getUserById(userId);
        user.setDateUpdated(LocalDateTime.now());
        user.setProfilePicture(imageUrl);
        User updated = userRepository.save(user);

        log.info("Successfully update profile picture for user [%s] with id [%s]".formatted(updated.getUsername(), updated.getId()));
        mapUserToUserProfileInfo(updated);
    }

    public UserProfileInfo getUserProfileInfo(UUID userId) {
        User user = getUserById(userId);
        return mapUserToUserProfileInfo(user);
    }

    public void updateUsername(UUID userId, String username) {
        User user = getUserById(userId);
        user.setUsername(username);
        User updated = userRepository.save(user);

        log.info("Successfully update profile username for user [%s] with id [%s]".formatted(updated.getUsername(), updated.getId()));
        mapUserToUserProfileInfo(updated);
    }

    public void updateEmail(UUID userId, String email) {
        User user = getUserById(userId);
        user.setEmail(email);
        User updated = userRepository.save(user);

        log.info("Successfully update profile email for user [%s] with id [%s]".formatted(updated.getUsername(), updated.getId()));
        mapUserToUserProfileInfo(updated);
    }

    public void updatePassword(UUID userId, String password) {
        User user = getUserById(userId);
        user.setPassword(passwordEncoder.encode(password));
        User updated = userRepository.save(user);

        log.info("Successfully update profile password for user [%s] with id [%s]".formatted(updated.getUsername(), updated.getId()));
        mapUserToUserProfileInfo(updated);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public List<UserWithRole> getAll() {
        return userRepository.findAll()
                .stream()
                .map(DtoMapper::mapUserToUserWithRole)
                .toList();
    }

    public UserProfileInfo getUserProfileInfoById(UUID userId) {
        User user = getUserById(userId);
        return mapUserToUserProfileInfo(user);
    }

    public boolean updateUserRole(UUID userId, String newRole) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setRole(Role.valueOf(newRole.toUpperCase()));
            userRepository.save(user);
            return true;
        }

        return false;
    }

    public boolean updateUserStatus(UUID userId, boolean isActive) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setIsActive(isActive);
            userRepository.save(user);
            return true;
        }

        return false;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = this.userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User with username " + username + " not found"));

        return new CustomUserDetails(user.getId(), user.getUsername(),user.getPassword(), user.getRole(), user.getIsActive());
    }

    public void updateLastLogin(String username) {
        User user = getUserByUsername(username);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    private User initializeUser(RegisterRequest registerRequest) {
        return User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .profilePicture("/images/default-profile.png")
                .dateRegistered(LocalDateTime.now())
                .role(Role.USER) //every new user has user role by default
                .build();
    }
}