package com.lemondead1.carshopservice;

import com.lemondead1.carshopservice.config.MainConfig;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Server;
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;
import org.springframework.web.SpringServletContainerInitializer;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

@Slf4j
public class CarShopServiceApplication {
  public static void main(String[] args) throws Exception {
    var container = new ServletContextHandler();
    container.setContextPath("/");
    container.addServletContainerInitializer(new SpringServletContainerInitializer(),
                                             AnnotationConfigDispatcherServletInitializer.class,
                                             SecurityWebApplicationInitializer.class);

    var server = new Server(8080);
    server.setHandler(container);
    server.start();
    server.join();
  }

  public static class SecurityWebApplicationInitializer extends AbstractSecurityWebApplicationInitializer { }

  public static class AnnotationConfigDispatcherServletInitializer
      extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
      return null;
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
      return new Class<?>[] { MainConfig.class };
    }

    @Override
    protected String[] getServletMappings() {
      return new String[] { "/" };
    }
  }
}