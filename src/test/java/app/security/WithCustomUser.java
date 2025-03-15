package app.security;

import org.springframework.security.test.context.support.WithSecurityContext;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@WithSecurityContext(factory = CustomUserSecurityContextFactory.class)
public @interface WithCustomUser {
    String username() default "testuser";
    String role() default "USER";
    String id() default "f8d4af7f-9d27-4d34-b445-7dbf4c96103f";
}
