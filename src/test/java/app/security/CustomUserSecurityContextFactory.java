package app.security;

import app.user.model.Role;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import java.util.UUID;

// The issue is that @WithMockUser creates a simple UsernamePasswordAuthenticationToken with a default User (Spring Security's org.springframework.security.core.userdetails.User), which does not use CustomUserDetails implementation
// I will use this in many tests, that's why I chose it instead of manually setting the SecurityContext in every test:
// UserDetails userDetails = new CustomUserDetails(testCommentOwner.getId(), "commentOwner", "password", Role.USER, true);
//    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//    SecurityContextHolder.getContext().setAuthentication(authentication);
public class CustomUserSecurityContextFactory implements WithSecurityContextFactory<WithCustomUser> {
    @Override
    public SecurityContext createSecurityContext(WithCustomUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        UUID userId = UUID.fromString(annotation.id());
        CustomUserDetails userDetails = new CustomUserDetails(userId, annotation.username(), "password", Role.valueOf(annotation.role()), true);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        context.setAuthentication(authentication);
        return context;
    }
}
