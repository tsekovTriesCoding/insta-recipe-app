package app.scheduler;

import app.user.model.User;
import app.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class UserStatusScheduler {

    private final UserRepository userRepository;

    public UserStatusScheduler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Scheduled(cron = "0 0 0 * * ?") // Runs daily at midnight
    public void deactivateInactiveUsers() {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);

        List<User> inactiveUsers = userRepository.findInactiveUsers(sixMonthsAgo);

        if (inactiveUsers.isEmpty()) {
            return;
        }

        for (User user : inactiveUsers) {
            user.setIsActive(false);
        }

        userRepository.saveAll(inactiveUsers);

        log.info("Deactivated {} inactive users.", inactiveUsers.size());
    }
}