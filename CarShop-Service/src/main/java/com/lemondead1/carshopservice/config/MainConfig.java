package com.lemondead1.carshopservice.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lemondead1.carshopservice.util.HasIdModule;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import java.util.List;
import java.util.Objects;
import java.util.Properties;

@ComponentScan("com.lemondead1.carshopservice")
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableSpringDataWebSupport
public class MainConfig {
  @Bean
  ObjectMapper objectMapper() {
    var objectMapper = new ObjectMapper();
    objectMapper.registerModule(new HasIdModule());
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    objectMapper.enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return objectMapper;
  }

  @Bean
  PropertySource<?> applicationPropertySource() {
    YamlPropertiesFactoryBean yamlProperties = new YamlPropertiesFactoryBean();
    yamlProperties.setResources(new ClassPathResource("application.yaml"));
    Properties properties = yamlProperties.getObject();
    Objects.requireNonNull(properties, "Properties cannot be null.");
    return new PropertiesPropertySource("config_file", properties);
  }

  @Bean
  PropertySources propertySources(List<PropertySource<?>> propertySourceList) {
    var propertySources = new MutablePropertySources();
    for (var propertySource : propertySourceList) {
      propertySources.addLast(propertySource);
    }
    return propertySources;
  }

  @Bean
  PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer(PropertySources propertySources) {
    var configurer = new PropertySourcesPlaceholderConfigurer();
    configurer.setPropertySources(propertySources);
    return configurer;
  }
}
