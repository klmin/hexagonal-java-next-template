package com.example.template.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    @Bean
    OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info().title("Backend API").version("v1"))
                // 실행 포트가 스펙에 들어가지 않게 고정 (openapi/openapi.json 비교용)
                .servers(List.of(new Server().url("/")));
    }
}
