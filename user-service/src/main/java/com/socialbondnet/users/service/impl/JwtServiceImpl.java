package com.socialbondnet.users.service.impl;

import com.socialbondnet.users.entity.Roles;
import com.socialbondnet.users.entity.Users;
import com.socialbondnet.users.service.IJwtService;
import org.apache.catalina.User;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;
@Service
public class JwtServiceImpl implements IJwtService {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(Users user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());

        List<String> roleNames = Optional.ofNullable(user.getRoles())
                .map(roles -> roles.stream()
                    .map(Roles::getRoleName)
                    .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
        claims.put("roles", roleNames);
        claims.put("email", user.getEmail());

        claims.put("permissions", getPermissionsByRoles(roleNames));

        return createToken(claims, user.getEmail());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
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
