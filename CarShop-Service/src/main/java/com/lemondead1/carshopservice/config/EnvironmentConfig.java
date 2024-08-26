package com.lemondead1.carshopservice.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.ClassPathResource;

import java.util.Objects;
import java.util.Properties;

@Configuration
public class EnvironmentConfig implements EnvironmentAware {
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
   * Manually loads YAML configuration and injects it into the environment.
   */
  @Override
  public void setEnvironment(Environment environment) {
    YamlPropertiesFactoryBean yamlProperties = new YamlPropertiesFactoryBean();
    yamlProperties.setResources(new ClassPathResource("application.yaml"));
    Properties properties = yamlProperties.getObject();
    Objects.requireNonNull(properties, "Properties cannot be null.");
    var configFileProperties = new PropertiesPropertySource("config_file", properties);
    ((ConfigurableEnvironment) environment).getPropertySources().addLast(configFileProperties);
  }

  /**
   * Adding support for placeholder values.
   */
  @Bean
  PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer(ConfigurableEnvironment environment) {
    var configurer = new PropertySourcesPlaceholderConfigurer();
    configurer.setPropertySources(environment.getPropertySources());
    return configurer;
  }
}
