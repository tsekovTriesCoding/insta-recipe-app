package app.web.dto;

import app.category.model.CategoryName;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Builder
@Data
public class CategoryShort {
    private UUID id;
    private CategoryName name;
}
