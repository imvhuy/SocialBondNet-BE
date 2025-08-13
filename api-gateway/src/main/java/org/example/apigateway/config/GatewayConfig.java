package org.example.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service", r -> r.path("/api/users/**")
                        .filters(f -> f
                                // Bỏ stripPrefix hoặc set = 0
                                // .stripPrefix(1)
                                .circuitBreaker(config -> config
                                        .setName("userServiceCB")
                                        .setFallbackUri("forward:/fallback/users")))
                        .uri("lb://user-service"))
                .build();
    }
}
