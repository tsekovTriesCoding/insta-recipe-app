package app.cloudinary.dto;

import lombok.Data;

@Data
public class ImageUploadResult {
    private final String imageUrl;
    private final String publicId;
}