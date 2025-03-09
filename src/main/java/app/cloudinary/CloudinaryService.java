package app.cloudinary;

import app.exception.ImageUploadException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * Uploads a file to Cloudinary and returns the secure URL.
     *
     * @param file the MultipartFile to upload.
     * @return the secure URL of the uploaded image.
     */

    public ImageUploadResult uploadImage(MultipartFile file) {
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            return new ImageUploadResult(
                    uploadResult.get("secure_url").toString(),
                    uploadResult.get("public_id").toString()
            );
        } catch (IOException e) {
            throw new ImageUploadException("Failed to upload image to Cloudinary");
        }
    }

    // Delete an image using its public ID
    public void deleteImage(String publicId) {
        if (publicId == null || publicId.isEmpty()) {
            return; // Nothing to delete
        }
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new ImageUploadException("Failed to delete image from Cloudinary");
        }
    }
}