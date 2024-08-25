package com.lemondead1.carshopservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@EnableWebMvc
// Enabling swagger docs.
@ComponentScan({ "org.springdoc", "io.swagger" })
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
  private final ObjectMapper objectMapper;

  /**
   * Configuring converters.
   *
   * @param converters initially an empty list of converters
   */
  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    // Needed for Swagger to work correctly.
    converters.add(new ByteArrayHttpMessageConverter());

    // Spring should configure it by default, but it doesn't for some reason.
    converters.add(new MappingJackson2HttpMessageConverter(objectMapper));
  }

  /**
   * Exposes swagger resources.
   */
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
    registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
  }

  /**
   * Customizing Swagger model resolver with a custom objectMapper.
   */
  @Bean
  ModelResolver modelResolver() {
    return new ModelResolver(objectMapper);
  }

  /**
   * Configures swagger api description.
   */
  @Bean
  OpenAPI openAPI() {
    return new OpenAPI().addSecurityItem(new SecurityRequirement().addList("basicAuth"))
                        .info(new Info().version("1.0.0")
                                        .title("CarShop Service API")
                                        .description("The API spec for the YLAB homework project."))
                        .components(new Components().addSecuritySchemes(
                            "basicAuth", new SecurityScheme().scheme("basic").type(SecurityScheme.Type.HTTP)
                        ));
  }
}
