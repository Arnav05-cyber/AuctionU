package org.example.filters;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    public static final List<String> openApiEndpoints = Arrays.asList(
            "/auth/v1/signup",
            "/auth/v1/login",
            "/auth/v1/refreshToken",
            "/eureka"
    );

    public Predicate<ServerHttpRequest> isSecure =
            request -> !request.getMethod().name().equals("OPTIONS") && openApiEndpoints.stream().noneMatch(uri -> request.getURI().getPath().contains(uri));

    

}
