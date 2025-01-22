package app.vallidation;

import app.vallidation.annotation.ImageFileType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class ImageFileTypeValidator implements ConstraintValidator<ImageFileType, MultipartFile> {
    private String[] allowedTypes;

    @Override
    public void initialize(ImageFileType constraintAnnotation) {
        this.allowedTypes = constraintAnnotation.allowedTypes();
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null) {
            return true;
        }

        String contentType = file.getContentType();
        for (String type : allowedTypes) {
            if (type.equalsIgnoreCase(contentType)) {
                return true;
            }
        }
        return false;
    }
}
