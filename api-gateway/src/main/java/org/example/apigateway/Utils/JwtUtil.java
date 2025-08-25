package org.example.apigateway.Utils;

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

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
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
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
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