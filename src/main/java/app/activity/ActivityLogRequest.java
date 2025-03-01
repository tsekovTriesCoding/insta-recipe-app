package app.activity;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ActivityLogRequest {
    private UUID userId;
    private String action;
}
