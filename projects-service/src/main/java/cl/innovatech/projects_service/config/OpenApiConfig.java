package cl.innovatech.projects_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Projects Service API")
                        .description("Microservicio de gestion y seguimiento de proyectos - InnovaTech DuocUC DSY1106-003V")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo InnovaTech")
                                .email("dev@innovatech.cl")));
    }
}
