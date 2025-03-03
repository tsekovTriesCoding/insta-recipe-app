package app.user.service;

import app.activity.ActivityLogService;
import app.cloudinary.CloudinaryService;
import app.exception.UserAlreadyExistsException;
import app.exception.UserNotFoundException;
import app.security.CustomUserDetails;
import app.user.model.Role;
import app.user.model.User;
import app.user.repository.UserRepository;
import app.web.dto.RegisterRequest;
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
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;
    private final ActivityLogService activityLogService;

    public void register(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new UserAlreadyExistsException("User with username " + registerRequest.getUsername() + " already exists.");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + registerRequest.getEmail() + " already exists.");
        }

        User user = userRepository.save(initializeUser(registerRequest));

        activityLogService.logActivity("You have successfully registered" , user.getId());

        log.info("Successfully create new user account for username [%s] and email [%s], with id [%s]"
                .formatted(user.getUsername(), user.getEmail(), user.getId()));
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

        activityLogService.logActivity("You have successfully updated your profile picture", updated.getId());

        log.info("Successfully update profile picture for user [%s] with id [%s]".formatted(updated.getUsername(), updated.getId()));
    }

    public void updateUsername(UUID userId, String username) {
        User user = getUserById(userId);
        user.setUsername(username);
        User updated = userRepository.save(user);

        activityLogService.logActivity("You have successfully updated your username", updated.getId());

        log.info("Successfully update profile username for user [%s] with id [%s]".formatted(updated.getUsername(), updated.getId()));
    }

    public void updateEmail(UUID userId, String email) {
        User user = getUserById(userId);
        user.setEmail(email);
        User updated = userRepository.save(user);

        activityLogService.logActivity("You have successfully updated your email", updated.getId());

        log.info("Successfully update profile email for user [%s] with id [%s]".formatted(updated.getUsername(), updated.getId()));
    }

    public void updatePassword(UUID userId, String password) {
        User user = getUserById(userId);
        user.setPassword(passwordEncoder.encode(password));
        User updated = userRepository.save(user);

        activityLogService.logActivity("You have successfully updated your password", updated.getId());

        log.info("Successfully update profile password for user [%s] with id [%s]".formatted(updated.getUsername(), updated.getId()));
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public void updateUserRole(UUID userId) {
        User user = getUserById(userId);

        Role newRole = null;

        if (user.getRole() == Role.ADMIN) {
            newRole = Role.USER;
        } else if (user.getRole() == Role.USER) {
            newRole = Role.ADMIN;
        }

        user.setRole(newRole);
        userRepository.save(user);
    }

    public void updateUserStatus(UUID userId) {
        User user = getUserById(userId);

        user.setIsActive(!user.getIsActive());
        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = this.userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User with username " + username + " not found"));

        return new CustomUserDetails(user.getId(), user.getUsername(), user.getPassword(), user.getRole(), user.getIsActive());
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