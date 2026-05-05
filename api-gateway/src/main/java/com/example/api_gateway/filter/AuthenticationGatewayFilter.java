package com.example.api_gateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuthenticationGatewayFilter extends AbstractGatewayFilterFactory<AuthenticationGatewayFilter.Config> {

    private final WebClient.Builder webClientBuilder;

    public AuthenticationGatewayFilter(WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            log.info("Checking auth for: {}", path);

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid authorization header");
                return handleUnauthorized(exchange, "Missing or invalid authorization header");
            }

            return validateToken(authHeader)
                    .flatMap(isValid -> {
                        if (isValid) {
                            return chain.filter(exchange);
                        } else {
                            log.warn("Invalid token rejected for path: {}", path);
                            return handleUnauthorized(exchange, "Invalid token");
                        }
                    })
                    .onErrorResume(error -> {
                        log.error("Auth service error for path {}: {}", path, error.getMessage());
                        return handleUnauthorized(exchange, "Authentication service error");
                    });
        };
    }

    private Mono<Boolean> validateToken(String authHeader) {
        return webClientBuilder.build()
                .post()
                .uri("lb://spring-auth/api/auth/validate-token")
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToMono(TokenValidationResponse.class)
                .map(TokenValidationResponse::isValid)
                .onErrorReturn(false);
    }

    private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");

        String body = String.format(
                "{\"error\":\"Unauthorized\",\"message\":\"%s\",\"authRequired\":true,\"loginUrl\":\"/api/auth/login\",\"timestamp\":%d}",
                message, System.currentTimeMillis()
        );

        org.springframework.core.io.buffer.DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
        // Configuration properties if needed
    }

    public static class TokenValidationResponse {
        private boolean valid;
        private String username;
        private String message;

        // Getters and setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
