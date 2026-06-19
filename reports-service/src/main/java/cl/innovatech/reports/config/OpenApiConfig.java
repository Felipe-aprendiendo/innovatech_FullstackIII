package cl.innovatech.reports.config;

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
                        .title("Reports Service API")
                        .description("Microservicio de reportes y KPIs consolidados - InnovaTech DuocUC DSY1106-003V")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo InnovaTech")
                                .email("dev@innovatech.cl")));
    }
}
