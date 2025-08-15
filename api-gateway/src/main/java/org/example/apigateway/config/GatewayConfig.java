package org.example.apigateway.config;

import org.example.apigateway.filter.AuthorizationGatewayFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    private final AuthorizationGatewayFilter authFilter;

    public GatewayConfig(AuthorizationGatewayFilter authFilter) {
        this.authFilter = authFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service Routes
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .filters(f -> f
                                .filter(authFilter.apply(new AuthorizationGatewayFilter.Config()))
                                .circuitBreaker(config -> config
                                        .setName("user-service-cb")
                                        .setFallbackUri("forward:/fallback/users")))
                        .uri("lb://user-service"))

                // Auth Service Routes (public, không cần auth filter)
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("auth-service-cb")
                                        .setFallbackUri("forward:/fallback/auth")))
                        .uri("lb://user-service"))

                // Future Post Service Routes (chuẩn bị sẵn)
                .route("post-service", r -> r
                        .path("/api/posts/**")
                        .filters(f -> f
                                .filter(authFilter.apply(new AuthorizationGatewayFilter.Config()))
                                .circuitBreaker(config -> config
                                        .setName("post-service-cb")
                                        .setFallbackUri("forward:/fallback/posts")))
                        .uri("lb://post-service"))

                // Admin Service Routes (chuẩn bị sẵn)
                .route("admin-service", r -> r
                        .path("/api/admin/**")
                        .filters(f -> f
                                .filter(authFilter.apply(new AuthorizationGatewayFilter.Config()))
                                .circuitBreaker(config -> config
                                        .setName("admin-service-cb")
                                        .setFallbackUri("forward:/fallback/admin")))
                        .uri("lb://admin-service"))

                .build();
    }
}
