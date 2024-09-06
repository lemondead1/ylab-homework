package com.lemondead1.audit.config;

import com.lemondead1.audit.Auditor;
import com.lemondead1.audit.aspect.AuditedAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnBean(Auditor.class)
public class AuditAutoConfiguration {
  @Bean
  AuditedAspect auditedAspect(Auditor auditor) {
    return new AuditedAspect(auditor);
  }
}
