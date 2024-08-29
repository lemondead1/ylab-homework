package com.lemondead1.carshopservice.config;

import com.lemondead1.carshopservice.util.ConstraintSecurityHandlerBuilder;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.EnumSet;
import java.util.List;

import static com.lemondead1.carshopservice.enums.UserRole.*;

@Configuration
@EnableAspectJAutoProxy
public class MainConfig {
  /**
   * Configuring security for the servlet context.
   */
  @Bean
  SecurityHandler securityHandler(LoginService loginService) {
    //@formatter:off
    return new ConstraintSecurityHandlerBuilder(loginService)
        .at("/users").with("POST").permit(ADMIN)
        .at("/users/search").with("POST").permit(MANAGER, ADMIN)
        .at("/users/me").with("GET", "PATCH").permit(CLIENT, MANAGER, ADMIN)
        .at("/users/*").with("GET").permit(CLIENT, MANAGER, ADMIN)
                       .with("PATCH", "DELETE").permit(ADMIN)

        .at("/cars").with("POST").permit(ADMIN)
        .at("/cars/search").with("POST").permit(CLIENT, MANAGER, ADMIN)
        .at("/cars/*").with("GET").permit(CLIENT, MANAGER, ADMIN)
                      .with("POST", "DELETE").permit(MANAGER, ADMIN)

        .at("/orders").with("POST").permit(CLIENT, MANAGER, ADMIN)
        .at("/orders/search").with("POST").permit(MANAGER, ADMIN)
        .at("/orders/*").with("GET", "POST").permit(CLIENT, MANAGER, ADMIN)
                        .with("DELETE").permit(ADMIN)

        .at("/signup").with("POST").permitAll()

        .build();
    //@formatter:on
  }

  /**
   * Doing some hacking to make Spring and Jetty work together.
   * Adds security to the servlet.
   */
  @Bean
  ServletContextInitializer servletContextInitializer(SecurityHandler securityHandler, List<Filter> filters) {
    return context -> {
      var jetty = (ServletContextHandler) ((ServletContextHandler.ServletContextApi) context).getContextHandler();
      jetty.setSecurityHandler(securityHandler);
    };
  }
}
