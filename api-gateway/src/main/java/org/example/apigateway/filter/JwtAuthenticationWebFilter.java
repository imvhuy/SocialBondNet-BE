package org.example.apigateway.filter;

import lombok.RequiredArgsConstructor;
import org.example.apigateway.Utils.JwtUtil;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationWebFilter implements WebFilter {
    private final JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwtToken = authHeader.substring(7);

            try {
                if (jwtUtil.validateToken(jwtToken)) {
                    String username = jwtUtil.extractUsername(jwtToken);
                    String role = jwtUtil.extractRole(jwtToken);
                    UUID userId = jwtUtil.extractUserId(jwtToken);

                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                    authorities.add(new SimpleGrantedAuthority(roleWithPrefix));

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);

                    // Thêm headers X-User-Id và X-Username vào request
                    ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate();
                    if (userId != null) {
                        requestBuilder.header("X-User-Id", userId.toString());
                    }
                    if (username != null) {
                        requestBuilder.header("X-Username", username);
                    }

                    ServerHttpRequest modifiedRequest = requestBuilder.build();
                    ServerWebExchange modifiedExchange = exchange.mutate().request(modifiedRequest).build();

                    return chain.filter(modifiedExchange)
                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
                }
            } catch (Exception e) {
                System.out.println("JWT validation failed: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return chain.filter(exchange);
    }
}
