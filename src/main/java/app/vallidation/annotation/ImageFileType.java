package app.vallidation.annotation;

import app.vallidation.ImageFileTypeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ImageFileTypeValidator.class)
public @interface ImageFileType {
    String message() default "Invalid file type. Only image files are allowed.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String[] allowedTypes() default {"image/jpeg", "image/png", "image/gif"};
}
