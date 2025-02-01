package app.web.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
public class RecipeForAdminPageInfo {
    private UUID id;
    private String title;
    private String author;
    private LocalDateTime createdDate;
}
