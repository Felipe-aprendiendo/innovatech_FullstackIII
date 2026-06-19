package cl.innovatech.users.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Users Service API")
                        .description("Microservicio de usuarios, roles y permisos - InnovaTech DuocUC DSY1106-003V")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo InnovaTech")
                                .email("dev@innovatech.cl")));
    }
}
