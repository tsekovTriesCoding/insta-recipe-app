package app.web.dto;

import app.vallidation.annotation.FileSize;
import app.vallidation.annotation.ImageFileType;
import app.vallidation.annotation.NotEmptyFile;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ChangeProfilePicture {
    @FileSize(maxSize = 3145728, message = "File size must be less than 3MB")
    @ImageFileType
    @NotEmptyFile
    private MultipartFile profilePicture;
}
