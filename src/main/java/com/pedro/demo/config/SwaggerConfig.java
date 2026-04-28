package com.pedro.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Yogur Maker")
                        .version("1.0.0")
                        .description("API REST para la gestión de producción de yogur")
                        .contact(new Contact()
                                .name("Pedro López")
                                .email("pedro@demo.com")));
    }
}
