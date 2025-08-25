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
                // Auth Service Routes (public, không cần auth filter)
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("auth-service-cb")
                                        .setFallbackUri("forward:/fallback/auth")))
                        .uri("lb://user-service"))

                // Profile Service Routes (semi-public, không cần auth filter)
                // Vì profile có thể xem public, service sẽ quyết định hiển thị gì
                .route("profile-service", r -> r
                        .path("/api/profile/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("profile-service-cb")
                                        .setFallbackUri("forward:/fallback/profile")))
                        .uri("lb://user-service"))

                // User Service Routes (protected, cần auth filter)
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .filters(f -> f
                                .filter(authFilter.apply(new AuthorizationGatewayFilter.Config()))
                                .circuitBreaker(config -> config
                                        .setName("user-service-cb")
                                        .setFallbackUri("forward:/fallback/users")))
                        .uri("lb://user-service"))

                // File Service Routes (public for serving uploaded files)
                .route("file-service", r -> r
                        .path("/files/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("file-service-cb")
                                        .setFallbackUri("forward:/fallback/files")))
                        .uri("lb://user-service"))

                .build();
    }
}
