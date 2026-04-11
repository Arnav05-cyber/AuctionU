package org.example.filters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Consumer;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator validator;

    @Autowired
    private JwtUtils jwtUtils;

    public AuthenticationFilter() {
        super(Config.class);
    }

    public static class Config {

    }


    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            if(validator.isSecure.test(exchange.getRequest())) {

                if (!exchange.getRequest().getHeaders().containsKey("Authorization")) {
                    throw new RuntimeException("Missing authorization header");
                }

                String header = exchange.getRequest().getHeaders().getFirst("Authorization");
                if (header != null && header.startsWith("Bearer ")) {
                    header = header.substring(7);
                }

                try {
                    jwtUtils.validateToken(header);

                    String userId = jwtUtils.extractUserId(header);

                    exchange.getRequest().mutate()
                            .header("X-User-Id", userId)
                            .build();
                } catch (Exception e) {
                    return onError(exchange, "Invalid authorization header", HttpStatus.UNAUTHORIZED);
                }
            }

            return chain.filter(exchange);
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus){
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);

        return response.setComplete();
    }
}
