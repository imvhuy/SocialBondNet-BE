package com.socialbondnet.users.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
@Component
public class UserContextUtil {
    public Long getCurrentUserId() {
        HttpServletRequest request = getCurrentRequest();
        String userIdHeader = request.getHeader("X-User-Id");
        return userIdHeader != null ? Long.valueOf(userIdHeader) : null;
    }

    public String getCurrentUsername() {
        HttpServletRequest request = getCurrentRequest();
        return request.getHeader("X-Username");
    }

    public String getCurrentUserRole() {
        HttpServletRequest request = getCurrentRequest();
        return request.getHeader("X-User-Role");
    }

    public boolean isTokenValid() {
        HttpServletRequest request = getCurrentRequest();
        return "true".equals(request.getHeader("X-Token-Valid"));
    }

    public boolean isAdmin() {
        return "ADMIN".equals(getCurrentUserRole());
    }

    public boolean isUser() {
        String role = getCurrentUserRole();
        return "USER".equals(role) || "ADMIN".equals(role);
    }

    private HttpServletRequest getCurrentRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
    }
}
