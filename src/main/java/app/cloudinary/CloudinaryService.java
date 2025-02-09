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
    public String uploadImage(MultipartFile file) {
        // Upload options: I can add some option I could like... maybe custom name
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new ImageUploadException("Failed to upload image to Cloudinary");
        }
    }
}
