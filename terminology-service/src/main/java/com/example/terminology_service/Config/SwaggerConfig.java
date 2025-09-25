package com.example.terminology_service.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NAMASTE Terminology Service API")
                        .description("Traditional Medicine to ICD-11 TM2 Code Mapping Service")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("NAMASTE Team")
                                .email("support@namaste.org"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server().url("http://localhost:8082").description("Development server"),
                        new Server().url("https://api.namaste.org").description("Production server")
                ));
    }
}
