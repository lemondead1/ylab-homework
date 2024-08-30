package com.lemondead1.logging.config;

import org.springframework.aop.Pointcut;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@ComponentScan("com.lemondead1.logging")
public class LoggingConfig {
  @Bean
  @Qualifier("configuredTimedClasses")
  Pointcut loggingPointcut(@Value("${lemondead1.logging.classes:}") String[] packages) {
    return new ComposablePointcut(clazz -> Arrays.stream(packages)
                                                 .anyMatch(pkg -> clazz.getName().startsWith(pkg)));
  }
}
