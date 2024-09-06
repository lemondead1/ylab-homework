package com.lemondead1.logging;

import com.lemondead1.logging.config.LoggingConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(LoggingConfig.class)
public @interface EnableLogging { }
