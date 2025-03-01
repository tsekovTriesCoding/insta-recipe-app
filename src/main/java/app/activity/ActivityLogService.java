package app.activity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
public class ActivityLogService {

    private final ActivityLogClient activityLogClient;

    public void logActivity(String action, UUID userId) {
        ActivityLogRequest request = new ActivityLogRequest(userId, action);

        activityLogClient.logActivity(request);

        log.info("Successfully logged activity - {} for user with id {}", action, userId);
    }

    public List<ActivityLogResponse> getActivityLog(UUID userId) {
        ResponseEntity<List<ActivityLogResponse>> activityLog = activityLogClient.getActivityLog(userId);

        return activityLog.getBody();
    }

    public void deleteLogsByUserId(UUID userId) {
        ResponseEntity<String> stringResponseEntity = activityLogClient.clearUserLogs(userId);

        String body = stringResponseEntity.getBody();

        log.info(body);
    }
}