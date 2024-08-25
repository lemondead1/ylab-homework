package com.lemondead1.carshopservice.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;
import org.springframework.core.io.ClassPathResource;

import java.util.List;
import java.util.Objects;
import java.util.Properties;

@Configuration
public class EnvironmentConfig {
  /**
   * Configuring an ObjectMapper, since Spring by default does not expose it as a bean.
   */
  @Bean
  @VisibleForTesting
  public static ObjectMapper objectMapper() {
    var objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    objectMapper.enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return objectMapper;
  }

  /**
   * Manually loads YAML configuration, since, apparently, it is not supported out of the box.
   */
  @Bean
  PropertySource<?> applicationPropertySource() {
    YamlPropertiesFactoryBean yamlProperties = new YamlPropertiesFactoryBean();
    yamlProperties.setResources(new ClassPathResource("application.yaml"));
    Properties properties = yamlProperties.getObject();
    Objects.requireNonNull(properties, "Properties cannot be null.");
    return new PropertiesPropertySource("config_file", properties);
  }

  /**
   * Configuring PropertySources. Quite possibly some class in Spring already does it, but it fails to happen at the right time.
   */
  @Bean
  PropertySources propertySources(List<PropertySource<?>> propertySourceList) {
    var propertySources = new MutablePropertySources();
    for (var propertySource : propertySourceList) {
      propertySources.addLast(propertySource);
    }
    return propertySources;
  }

  /**
   * Adding support for placeholder values.
   */
  @Bean
  PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer(PropertySources propertySources) {
    var configurer = new PropertySourcesPlaceholderConfigurer();
    configurer.setPropertySources(propertySources);
    return configurer;
  }
}
