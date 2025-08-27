package com.socialbondnet.users.service.impl;

import com.socialbondnet.users.entity.Roles;
import com.socialbondnet.users.entity.Users;
import com.socialbondnet.users.repository.UserRepository;
import com.socialbondnet.users.service.IJwtService;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements IJwtService {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh-expiration:604800000}") // 7 days default
    private Long refreshExpiration;

    private final UserRepository userRepository;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    @Override
    public String generateAccessToken(Users user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("tokenType", "ACCESS");

        List<String> roleNames = Optional.ofNullable(user.getRoles())
                .map(roles -> roles.stream()
                    .map(Roles::getRoleName)
                    .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
        claims.put("roles", roleNames);
        claims.put("email", user.getEmail());
        claims.put("permissions", getPermissionsByRoles(roleNames));

        return createToken(claims, user.getEmail(), expiration);
    }

    @Override
    public String generateRefreshToken(Users user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("tokenType", "REFRESH");
        claims.put("email", user.getEmail());

        return createToken(claims, user.getEmail(), refreshExpiration);
    }

    // Keep the old method for backward compatibility
    public String generateToken(Users user) {
        return generateAccessToken(user);
    }

    private String createToken(Map<String, Object> claims, String subject, Long expirationTime) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public String extractEmailFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean isTokenValid(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return !claims.getExpiration().before(new Date()) &&
                   "ACCESS".equals(claims.get("tokenType"));
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isRefreshTokenValid(String refreshToken) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(refreshToken)
                    .getPayload();

            return !claims.getExpiration().before(new Date()) &&
                   "REFRESH".equals(claims.get("tokenType"));
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Users getUserFromRefreshToken(String refreshToken) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(refreshToken)
                    .getPayload();

            String email = claims.getSubject();
            return userRepository.findByEmail(email).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Users getUserFromAccessToken(String accessToken) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload();

            // Verify it's an access token
            if (!"ACCESS".equals(claims.get("tokenType"))) {
                return null;
            }

            String email = claims.getSubject();
            return userRepository.findByEmail(email).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private List<String> getPermissionsByRoles(List<String> roles) {
        Set<String> permissions = new HashSet<>();

        for (String role : roles) {
            switch (role) {
                case "ADMIN":
                    permissions.addAll(Arrays.asList("READ", "WRITE", "DELETE", "ADMIN"));
                    break;
                case "USER":
                    permissions.addAll(Arrays.asList("READ", "WRITE"));
                    break;
                default:
                    permissions.add("READ");
            }
        }

        return new ArrayList<>(permissions);
    }
}
