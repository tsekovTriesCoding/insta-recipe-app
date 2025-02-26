package app.scheduler;

import app.user.model.User;
import app.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserStatusSchedulerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserStatusScheduler userStatusScheduler;

    @Test
    void testDeactivateInactiveUsersWhenThereAreInactiveUsers() {
        User user1 = new User();
        user1.setId(UUID.randomUUID());
        user1.setIsActive(true);

        User user2 = new User();
        user2.setId(UUID.randomUUID());
        user2.setIsActive(true);

        List<User> inactiveUsers = List.of(user1, user2);

        when(userRepository.findInactiveUsers(any(LocalDateTime.class))).thenReturn(inactiveUsers);

        userStatusScheduler.deactivateInactiveUsers();

        assertFalse(user1.getIsActive());
        assertFalse(user2.getIsActive());

        verify(userRepository, times(1)).findInactiveUsers(any(LocalDateTime.class));
        verify(userRepository, times(1)).saveAll(inactiveUsers);
    }

    @Test
    void testDeactivateInactiveUsersWhenNoInactiveUsers() {
        when(userRepository.findInactiveUsers(any(LocalDateTime.class))).thenReturn(Collections.emptyList());

        userStatusScheduler.deactivateInactiveUsers();

        verify(userRepository, times(1)).findInactiveUsers(any(LocalDateTime.class));
        verify(userRepository, never()).saveAll(any());
    }
}