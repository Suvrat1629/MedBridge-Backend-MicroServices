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

            log.info("🔒 Checking authentication for: {}", path);

            // Extract Authorization header
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("❌ Missing authorization header");
                return handleUnauthorized(exchange, "Missing or invalid authorization header");
            }

            log.info("🔍 Validating token...");

            // Validate token with spring-auth service
            return validateToken(authHeader)
                    .flatMap(isValid -> {
                        if (isValid) {
                            log.info("✅ Token is valid - allowing request");
                            return chain.filter(exchange);
                        } else {
                            log.warn("❌ Token is invalid");
                            return handleUnauthorized(exchange, "Invalid token");
                        }
                    })
                    .onErrorResume(error -> {
                        log.error("💥 Authentication error: {}", error.getMessage());
                        return handleUnauthorized(exchange, "Authentication service error");
                    });
        };
    }

    private Mono<Boolean> validateToken(String authHeader) {
        log.info("🌐 Calling spring-auth service at: http://localhost:8084/token/validate");

        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:8084")
                .build();

        return webClient
                .post()
                .uri("/token/validate")
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToMono(TokenValidationResponse.class)
                .map(response -> {
                    log.info("🔍 Token validation response: valid={}, username={}",
                            response.isValid(), response.getUsername());
                    return response.isValid();
                })
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
