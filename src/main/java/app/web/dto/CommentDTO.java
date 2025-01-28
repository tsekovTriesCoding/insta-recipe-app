package app.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CommentDTO {
    private UUID id;
    private String content;
    private String createdBy;
    private LocalDateTime createdDate;
}
