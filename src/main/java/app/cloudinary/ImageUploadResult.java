package app.cloudinary;

import lombok.Data;

@Data
public class ImageUploadResult {
    private final String imageUrl;
    private final String publicId;
}