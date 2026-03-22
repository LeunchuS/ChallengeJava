package com.leandrocarron.challengejava.config;

import com.leandrocarron.challengejava.dto.ErrorDTO.ErrorResponseDTO;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.core.converter.ModelConverters;

import java.util.Map;


@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("ADMINISTRACIÓN DE TRANSACCIONES")
                        .description("Carga y consultas de transacciones")
                        .version("1.0.0"));

    }
}