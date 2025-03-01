package app.activity;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "activity-log-service", url = "${activity.log.service.url}")
public interface ActivityLogClient {

    @PostMapping("/api/v1/activity-log")
    ResponseEntity<ActivityLogResponse> logActivity(@RequestBody ActivityLogRequest request);

    @GetMapping("/api/v1/activity-log")
    ResponseEntity<List<ActivityLogResponse>> getActivityLog(@RequestParam(name = "userId") UUID userId);

    @DeleteMapping("/api/v1/activity-log")
    ResponseEntity<String> clearUserLogs(@RequestParam(name = "userId") UUID userId);
}
