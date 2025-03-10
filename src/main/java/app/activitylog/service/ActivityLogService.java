package app.activitylog.service;

import app.activitylog.client.ActivityLogClient;
import app.activitylog.dto.ActivityLogRequest;
import app.activitylog.dto.ActivityLogResponse;
import app.activitylog.event.ActivityLogEvent;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
public class ActivityLogService {

    private final ActivityLogClient activityLogClient;

    @EventListener
    public void handleActivityLogEvent(ActivityLogEvent event) {
        try {
            ActivityLogRequest request = ActivityLogRequest.builder()
                    .userId(event.getUserId())
                    .action(event.getAction())
                    .build();

            activityLogClient.logActivity(request);
            log.info("Successfully logged activity - {}", event.getAction());
        } catch (FeignException e) {
            log.error("Failed to log activity due to service unavailability: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while logging activity: {}", e.getMessage(), e);
        }
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