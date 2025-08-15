package org.example.apigateway.filter;

import org.example.apigateway.Utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
public class AuthorizationGatewayFilter extends AbstractGatewayFilterFactory<AuthorizationGatewayFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationGatewayFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    public AuthorizationGatewayFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            logger.info("Processing request for path: {}", path);

            // Skip cho public endpoints
            if (isPublicEndpoint(path)) {
                logger.info("Public endpoint, skipping authorization: {}", path);
                return chain.filter(exchange);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("Missing or invalid Authorization header for path: {}", path);
                // Không cần return error vì Spring Security sẽ xử lý
                return chain.filter(exchange);
            }

            String token = authHeader.substring(7);
            try {
                if (jwtUtil.validateToken(token)) {
                    String username = jwtUtil.extractUsername(token);
                    String role = jwtUtil.extractRole(token);
                    Object userId = jwtUtil.extractUserId(token);

                    logger.info("User: {}, Role: {}, accessing path: {}", username, role, path);

                    // Thêm user info vào headers cho downstream services
                    ServerHttpRequest modifiedRequest = request.mutate()
                            .header("X-User-Id", userId != null ? userId.toString() : "")
                            .header("X-Username", username)
                            .header("X-User-Role", role)
                            .header("Authorization", authHeader)
                            .build();

                    logger.info("Request enriched with user info for: {}", username);
                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                }
            } catch (Exception e) {
                logger.error("Error processing token for path: {}, error: {}", path, e.getMessage());
                // Để Spring Security xử lý authentication error
            }

            return chain.filter(exchange);
        };
    }

    private boolean isPublicEndpoint(String path) {
        List<String> publicPaths = Arrays.asList(
                "/api/auth/",     // Authentication endpoints
                "/actuator/",     // Actuator endpoints
                "/fallback/"      // Fallback endpoints
        );

        return publicPaths.stream().anyMatch(path::startsWith);
    }

    private boolean hasRequiredRole(String path, String token) {
        try {
            String role = jwtUtil.extractRole(token);

            // Admin có quyền truy cập tất cả
            if ("ADMIN".equals(role)) {
                return true;
            }

            // User endpoints - USER và ADMIN có thể truy cập
            if (path.startsWith("/api/users/")) {
                return "USER".equals(role) || "ADMIN".equals(role);
            }

            // Post endpoints (nếu có) - USER, MODERATOR, ADMIN
            if (path.startsWith("/api/posts/")) {
                return "USER".equals(role) || "MODERATOR".equals(role) || "ADMIN".equals(role);
            }

            // Admin endpoints (nếu có) - chỉ ADMIN
            if (path.startsWith("/api/admin/")) {
                return "ADMIN".equals(role);
            }

            // Moderator endpoints (nếu có) - MODERATOR và ADMIN
            if (path.startsWith("/api/moderator/")) {
                return "MODERATOR".equals(role) || "ADMIN".equals(role);
            }

            // Default: authenticated users có thể truy cập
            return true;

        } catch (Exception e) {
            logger.error("Error checking role for path: {}, error: {}", path, e.getMessage());
            return false;
        }
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().add("Content-Type", "application/json");

        String errorBody = String.format("{\"error\":\"%s\", \"status\":%d, \"timestamp\":\"%s\"}",
                err, httpStatus.value(), java.time.Instant.now().toString());
        var dataBuffer = response.bufferFactory().wrap(errorBody.getBytes());

        return response.writeWith(Mono.just(dataBuffer));
    }

    public static class Config {
    }
}
