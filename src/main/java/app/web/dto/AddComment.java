package app.web.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class AddComment {
    private UUID id;

    private String content;
}
