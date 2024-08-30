package com.lemondead1.carshopservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemondead1.carshopservice.conversion.HasIdToStringConverter;
import com.lemondead1.carshopservice.conversion.StringToHasIdEnumConverterFactory;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@EnableWebMvc
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
  private final ObjectMapper objectMapper;
  private final StringToHasIdEnumConverterFactory stringToHasIdEnumConverterFactory;
  private final HasIdToStringConverter hasIdToStringConverter;

  /**
   * Configuring converters.
   *
   * @param converters initially an empty list of converters
   */
  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    // Needed for Swagger to work correctly.
    converters.add(new ByteArrayHttpMessageConverter());

    // Now spring configures one of those, but it does not use the custom ObjectMapper.
    converters.add(new MappingJackson2HttpMessageConverter(objectMapper));
  }

  /**
   * Registers custom converters.
   */
  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverterFactory(stringToHasIdEnumConverterFactory);
    registry.addConverter(hasIdToStringConverter);
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
   * Manually exposing OpenAPI instance form the config file to fix the marvellous SpringDoc programming.
   */
  @Bean
  OpenAPI openAPI(SpringDocConfigProperties springDocConfigProperties) {
    return springDocConfigProperties.getOpenApi();
  }
}
