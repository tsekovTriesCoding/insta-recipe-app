package app.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
public class CommentByRecipe {
    private UUID id;
    private String content;
    private String createdBy;
    private LocalDateTime createdDate;
}
