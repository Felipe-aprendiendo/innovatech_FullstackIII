package cl.innovatech.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder, GatewayServicesProperties services) {
        return builder.routes()
                .route("auth-service", route -> route
                        .path("/api/v1/auth/**")
                        .uri(services.getAuth()))
                .route("users-service", route -> route
                        .path("/api/v1/users/**", "/api/v1/roles/**", "/api/v1/permissions/**")
                        .uri(services.getUsers()))
                .route("projects-service", route -> route
                        .path("/api/v1/projects/**")
                        .uri(services.getProjects()))
                .route("tasks-service", route -> route
                        .path("/api/v1/tasks/**")
                        .uri(services.getTasks()))
                .route("reports-service", route -> route
                        .path("/api/v1/reports/**")
                        .uri(services.getReports()))
                .build();
    }
}
