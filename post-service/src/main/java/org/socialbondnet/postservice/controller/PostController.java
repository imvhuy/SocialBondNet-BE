package org.socialbondnet.postservice.controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.socialbondnet.postservice.model.request.PostRequest;
import org.socialbondnet.postservice.model.response.PostResponse;
import org.socialbondnet.postservice.service.PostService;
import org.socialbondnet.postservice.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;
    @Value("${jwt-secret}")
    private String secretKey;
    @PostMapping
    public ResponseEntity<String> addPost(@RequestBody PostRequest postRequest, HttpServletRequest request) {
        String userId = getUserIdByToken(request);
        postRequest.setUserId(userId);
        return postService.addPost(postRequest);
    }
    private String getUserIdByToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Không tìm thấy token xác thực hoặc định dạng không hợp lệ");
        }
        String token = authHeader.substring(7);
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.get("userId", String.class);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
