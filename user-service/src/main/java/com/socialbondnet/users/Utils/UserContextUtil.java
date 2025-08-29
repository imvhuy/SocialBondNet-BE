package com.socialbondnet.users.Utils;

import org.springframework.stereotype.Component;

/**
 * Utility class để lấy thông tin user từ headers được gửi từ API Gateway
 */
@Component
public class UserContextUtil {

    /**
     * Lấy current user ID từ header X-User-Id
     */
    public static String getCurrentUserId(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID is required");
        }
        return userIdHeader.trim();
    }

    /**
     * Lấy current username từ header X-Username
     */
    public static String getCurrentUsername(String usernameHeader) {
        return usernameHeader != null ? usernameHeader.trim() : null;
    }

    /**
     * Lấy current user role từ header X-User-Role
     */
    public static String getCurrentUserRole(String roleHeader) {
        return roleHeader != null ? roleHeader.trim() : null;
    }

    /**
     * Kiểm tra user có phải admin không
     */
    public static boolean isAdmin(String roleHeader) {
        return "ADMIN".equals(getCurrentUserRole(roleHeader));
    }

    /**
     * Kiểm tra user có phải moderator không
     */
    public static boolean isModerator(String roleHeader) {
        String role = getCurrentUserRole(roleHeader);
        return "MODERATOR".equals(role) || "ADMIN".equals(role);
    }

    /**
     * Validate user có quyền truy cập resource không
     */
    public static boolean canAccessResource(String currentUserId, String resourceOwnerId, String roleHeader) {
        // Owner luôn có quyền truy cập
        if (currentUserId != null && currentUserId.equals(resourceOwnerId)) {
            return true;
        }

        // Admin có quyền truy cập tất cả
        return isAdmin(roleHeader);
    }
}
