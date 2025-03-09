package app.activitylog.aspect;

import app.activitylog.annotation.LogActivity;
import app.activitylog.service.ActivityLogService;
import app.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Aspect
@Component
public class ActivityLogAspect {

    private final ActivityLogService activityLogService;
    private final SpelExpressionParser spelParser;
    private final StandardEvaluationContext evalContext;

    @Around("@annotation(logActivity)")
    public Object logActivity(ProceedingJoinPoint joinPoint, LogActivity logActivity) throws Throwable {
        UUID userId = getCurrentUserId();
        if (userId == null) {
            return joinPoint.proceed(); // Skip logging if no user
        }

        Object result;
        try {
            result = joinPoint.proceed(); // Execute the original method
        } catch (Throwable ex) {
            // Optional: Handle/log failed activities if needed
            throw ex; // Preserve the original exception
        }

        // **Now that we have the result, bind it to SpEL**
        String activityMessage = parseLogMessage(joinPoint, logActivity.activity(), result);
        activityLogService.logActivity(activityMessage, userId);

        return result;
    }

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            return null; // User is not logged in
        }

        return ((CustomUserDetails) authentication.getPrincipal()).getId();
    }

    private String parseLogMessage(ProceedingJoinPoint joinPoint, String spelExpression, Object result) {
        // Get method argument names and values
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        // Bind method arguments to SpEL context if needed later
        if (parameterNames != null && args != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                evalContext.setVariable(parameterNames[i], args[i]);
            }
        }

        // **Bind the returned object (e.g., Comment,Recipe...)**
        if (result != null) {
            evalContext.setVariable("result", result); // You can use #result in SpEL
        }

        // Evaluate the SpEL expression and return the parsed message
        return spelParser.parseExpression(spelExpression).getValue(evalContext, String.class);
    }
}