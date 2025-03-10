package app.activitylog.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ActivityLogEvent {
    private UUID userId;
    private String action;
}