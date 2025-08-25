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

    // Danh sách endpoints public - không cần JWT
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/api/auth/",
            "/actuator/",
            "/fallback/"
    );

    public AuthorizationGatewayFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            logger.info("Processing request: {}", path);

            // Skip JWT validation cho public endpoints
            if (isPublicPath(path)) {
                logger.info("Public endpoint, skipping authentication: {}", path);
                return chain.filter(exchange);
            }

            // Kiểm tra Authorization header
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("Missing or invalid Authorization header for: {}", path);
                return handleUnauthorized(exchange, "Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7);

            try {
                // Validate JWT token
                if (!jwtUtil.validateToken(token)) {
                    logger.warn("Invalid JWT token for: {}", path);
                    return handleUnauthorized(exchange, "Invalid or expired token");
                }

                // Extract user information từ token
                String username = jwtUtil.extractUsername(token);
                String role = jwtUtil.extractRole(token);
                String userId = jwtUtil.extractUserId(token) != null ?
                        jwtUtil.extractUserId(token).toString() : username;

                logger.info("Authentication successful - User: {}, Role: {}", username, role);

                // Thêm user info vào headers để downstream services sử dụng
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-Id", userId)
                        .header("X-Username", username)
                        .header("X-User-Role", role)
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                logger.error("JWT processing error for {}: {}", path, e.getMessage());
                return handleUnauthorized(exchange, "Authentication failed");
            }
        };
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");

        String body = String.format(
                "{\"error\":\"%s\",\"status\":401,\"timestamp\":\"%s\",\"path\":\"%s\"}",
                message,
                java.time.Instant.now().toString(),
                exchange.getRequest().getURI().getPath()
        );

        var buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
        // Configuration class nếu cần
    }
}
