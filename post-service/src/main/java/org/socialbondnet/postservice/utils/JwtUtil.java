package org.socialbondnet.postservice.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt-secret}")
    private String secret;

    private SecretKey getSigningKey() {
        try {
            return Keys.hmacShaKeyFor(secret.getBytes());
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo signing key: " + e.getMessage());
        }
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    @SuppressWarnings("unchecked")
    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        Object roleObj = claims.get("roles");
        if (roleObj instanceof List) {
            List<String> roles = (List<String>) roleObj;
            return roles != null && !roles.isEmpty() ? roles.get(0) : null;
        }

        roleObj = claims.get("role");
        if (roleObj != null) {
            return roleObj.toString();
        }

        roleObj = claims.get("authorities");
        if (roleObj instanceof List) {
            List<String> authorities = (List<String>) roleObj;
            return authorities != null && !authorities.isEmpty() ?
                    authorities.get(0).replace("ROLE_", "") : null;
        }

        return null;
    }

    public UUID extractUserId(String token) {
        return extractClaim(token, claims -> {
            Object userId = claims.get("userId");
            return (userId != null) ? UUID.fromString(userId.toString()) : null;
        });
    }

    @SuppressWarnings("unchecked")
    public List<String> extractPermissions(String token) {
        return extractClaim(token, claims -> (List<String>) claims.get("permissions"));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("Token không được để trống");
        }

        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new RuntimeException("Token đã hết hạn");
        } catch (io.jsonwebtoken.security.SecurityException e) {
            throw new RuntimeException("Token không hợp lệ - Chữ ký không đúng");
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            throw new RuntimeException("Token không đúng định dạng JWT");
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            throw new RuntimeException("Token không được hỗ trợ");
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xử lý token: " + e.getMessage());
        }
    }

    public Boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("Token không được để trống");
        }

        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parse(token);

            return !isTokenExpired(token);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new RuntimeException("Token đã hết hạn");
        } catch (io.jsonwebtoken.security.SecurityException e) {
            throw new RuntimeException("Token không hợp lệ - Chữ ký không đúng");
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi đọc thông tin từ token: " + e.getMessage());
        }
    }

    private Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi kiểm tra hạn token: " + e.getMessage());
        }
    }

    public boolean hasRole(String token, String role) {
        String userRole = extractRole(token);
        return userRole != null && userRole.equals(role);
    }

    public boolean hasAnyRole(String token, String... roles) {
        String userRole = extractRole(token);
        if (userRole == null) return false;

        for (String role : roles) {
            if (userRole.equals(role)) return true;
        }
        return false;
    }

    public boolean hasPermission(String token, String permission) {
        List<String> permissions = extractPermissions(token);
        return permissions != null && permissions.contains(permission);
    }
}
