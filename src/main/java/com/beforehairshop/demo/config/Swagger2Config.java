package com.beforehairshop.demo.config;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

import java.util.List;

@Configuration
public class Swagger2Config {

//    @Bean
//    public GroupedOpenApi publicApi() {
//        return GroupedOpenApi.builder()
//                .group("v1-definition")
//                .pathsToMatch("/api/**")
//                .build();
//    }

    @Bean
    public OpenAPI springShopOpenAPI() {
        Info info = new Info()
                .title("Before Hairshop API")
                .version("0.0.1")
                .description("Demo API")
                .termsOfService("")
                .contact(new Contact().name("비포헤어샵").url("https://beforehairshop.com").email("beforehairshop@gmail.com"));

        return new OpenAPI()
                .info(info);
    }
}