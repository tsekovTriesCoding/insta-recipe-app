package app.config;

import app.activitylog.event.ActivityLogEvent;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.event.EventListener;

import java.util.ArrayList;
import java.util.List;

@TestConfiguration
public class EventCaptureConfig {
// Do not use @MockBean ApplicationEventPublisher in integration tests.
    //It replaces the real publisher with a mock, preventing real event propagation.
    //Mocking ApplicationEventPublisher in integration tests can bypass the event-driven nature of Spring, which isn't ideal for testing how the application reacts to events.

    private final List<ActivityLogEvent> capturedEvents = new ArrayList<>();

    @EventListener
    public void onActivityLogEvent(ActivityLogEvent event) {
        capturedEvents.add(event);
    }

    public void clearCapturedEvents() {
        capturedEvents.clear();
    }

    public List<ActivityLogEvent> getCapturedEvents() {
        return capturedEvents;
    }
}
