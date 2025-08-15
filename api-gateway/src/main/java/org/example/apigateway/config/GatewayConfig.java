package org.example.apigateway.config;

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
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .filters(f -> f
                                .filter(authFilter.apply(new AuthorizationGatewayFilter.Config())))
                        .uri("lb://user-service"))
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .uri("lb://user-service"))
                .build();
    }
}
