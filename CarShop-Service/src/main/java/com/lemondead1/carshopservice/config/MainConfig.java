package com.lemondead1.carshopservice.config;

import com.lemondead1.carshopservice.util.ConstraintSecurityHandlerBuilder;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.ServletContext;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.util.EnumSet;
import java.util.List;

import static com.lemondead1.carshopservice.enums.UserRole.*;

@Configuration
@EnableAspectJAutoProxy
public class MainConfig {
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

  @Bean
  ServletContextAware servletContextAware(WebApplicationContext spring,
                                          SecurityHandler securityHandler,
                                          List<Filter> filters) {
    return context -> {
      var jetty = (ServletContextHandler) ((ServletContextHandler.ServletContextApi) context).getContextHandler();
      jetty.addEventListener(new ContextLoaderListener(spring));
      jetty.setContextPath("/");
      jetty.setSecurityHandler(securityHandler);
      for (var filter : filters) {
        jetty.addFilter(filter, "/*", EnumSet.allOf(DispatcherType.class));
      }
      jetty.addServlet(new DispatcherServlet(spring), "/*");
    };
  }

  @Bean
  Server server(ServletContext contextHandler, @Value("${server.port}") int port) {
    var server = new Server(port);
    server.setHandler(((ServletContextHandler.ServletContextApi) contextHandler).getContextHandler());
    return server;
  }
}
