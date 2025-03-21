package app.activitylog;

import app.activitylog.client.ActivityLogClient;
import app.activitylog.dto.ActivityLogRequest;
import app.activitylog.dto.ActivityLogResponse;
import app.activitylog.event.ActivityLogEvent;
import app.activitylog.service.ActivityLogService;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityLogServiceTest {

    @Mock
    private ActivityLogClient activityLogClient;

    @InjectMocks
    private ActivityLogService activityLogService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    void handleActivityLogEvent_ShouldCallClientLogActivity() {
        ActivityLogEvent event = new ActivityLogEvent(userId, "TEST_ACTION");

        assertDoesNotThrow(() -> activityLogService.handleActivityLogEvent(event));
        verify(activityLogClient, times(1)).logActivity(any(ActivityLogRequest.class));
    }

    @Test
    void handleActivityLogEvent_ShouldHandleFeignException() {
        doThrow(FeignException.class).when(activityLogClient).logActivity(any(ActivityLogRequest.class));
        ActivityLogEvent event = new ActivityLogEvent(userId, "TEST_ACTION");

        assertDoesNotThrow(() -> activityLogService.handleActivityLogEvent(event));
        verify(activityLogClient, times(1)).logActivity(any(ActivityLogRequest.class));
    }

    @Test
    void getActivityLog_ShouldReturnListOfLogs() {
        List<ActivityLogResponse> mockResponse = Collections.singletonList(new ActivityLogResponse());
        when(activityLogClient.getActivityLog(userId)).thenReturn(ResponseEntity.ok(mockResponse));

        List<ActivityLogResponse> result = activityLogService.getActivityLog(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getActivityLog_ShouldHandleEmptyResponse() {
        when(activityLogClient.getActivityLog(userId)).thenReturn(ResponseEntity.ok(Collections.emptyList()));

        List<ActivityLogResponse> result = activityLogService.getActivityLog(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getActivityLog_ShouldHandleNullResponse() {
        when(activityLogClient.getActivityLog(userId)).thenReturn(null);

        assertThrows(NullPointerException.class, () -> activityLogService.getActivityLog(userId));
    }

    @Test
    void getActivityLog_ShouldThrow_WhenServiceIsDown() {
        doThrow(FeignException.class).when(activityLogClient).getActivityLog(userId);

        assertThrows(FeignException.class, () -> activityLogService.getActivityLog(userId));
        verify(activityLogClient, times(1)).getActivityLog(userId);
    }

    @Test
    void deleteLogsByUserId_ShouldCallClientClearUserLogs() {
        when(activityLogClient.clearUserLogs(userId)).thenReturn(ResponseEntity.ok("Logs cleared"));

        assertDoesNotThrow(() -> activityLogService.deleteLogsByUserId(userId));
        verify(activityLogClient, times(1)).clearUserLogs(userId);
    }

    @Test
    void deleteLogsByUserId_ShouldThrow_WhenServiceIsDown() {
        doThrow(FeignException.class).when(activityLogClient).clearUserLogs(userId);

        assertThrows(FeignException.class, () -> activityLogService.deleteLogsByUserId(userId));
        verify(activityLogClient, times(1)).clearUserLogs(userId);
    }
}
