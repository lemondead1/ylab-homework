package com.lemondead1.carshopservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class OpenAPIConfig {
  private final ObjectMapper objectMapper;

  /**
   * Customizing Swagger model resolver with a custom objectMapper.
   */
  @Bean
  ModelResolver modelResolver() {
    return new ModelResolver(objectMapper);
  }

  /**
   * Manually exposing OpenAPI instance form the config file to fix the marvellous SpringDoc programming.
   */
  @Bean
  OpenAPI openAPI(SpringDocConfigProperties springDocConfigProperties) {
    return springDocConfigProperties.getOpenApi();
  }
}
