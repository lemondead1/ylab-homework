package com.lemondead1.carshopservice;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Server;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

@Slf4j
public class CarShopServiceApplication {
  public static void main(String[] args) throws Exception {
    var spring = new AnnotationConfigWebApplicationContext();
    spring.setServletContext(new ServletContextHandler().getServletContext());

    spring.scan("com.lemondead1.carshopservice");
    spring.refresh();

    var server = spring.getBean(Server.class);
    server.start();
    server.join();
  }
}