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


            if (isPublicEndpoint(path)) {
                return chain.filter(exchange);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);
            try {
                if (!jwtUtil.validateToken(token)) {
                    return onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
                }

                String username = jwtUtil.extractUsername(token);
                String role = jwtUtil.extractRole(token);
                Object userId = jwtUtil.extractUserId(token);


                if (!hasRequiredRole(path, token)) {
                    return onError(exchange, "Insufficient permissions", HttpStatus.FORBIDDEN);
                }

                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-Id", userId != null ? userId.toString() : "")
                        .header("X-Username", username)
                        .header("X-User-Role", role)
                        .header("Authorization", authHeader)
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            } catch (Exception e) {
                return onError(exchange, "Error processing token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private boolean isPublicEndpoint(String path) {
        List<String> publicPaths = Arrays.asList(
                "/api/auth/", // Public authentication endpoints
                "/actuator/" // Actuator endpoints
        );

        return publicPaths.stream().anyMatch(path::startsWith);
    }

    private boolean hasRequiredRole(String path, String token) {
        if (path.contains("/api/users/")) {
            return jwtUtil.hasAnyRole(token, "USER");
        }
        return true;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().add("Content-Type", "application/json");

        String errorBody = String.format("{\"error\":\"%s\", \"status\":%d}", err, httpStatus.value());
        var dataBuffer = response.bufferFactory().wrap(errorBody.getBytes());

        return response.writeWith(Mono.just(dataBuffer));
    }

    public static class Config {
    }
}
