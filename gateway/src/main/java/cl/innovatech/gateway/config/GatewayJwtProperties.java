package cl.innovatech.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gateway.jwt")
public class GatewayJwtProperties {

    private String secret = "cambiar_por_un_secreto_de_al_menos_32_caracteres_muy_seguro";

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
