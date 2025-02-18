package app.web.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
public class CommentForAdminPage {
    private UUID id;
    private String author;
    private String content;
    private LocalDateTime createdDate;
}
