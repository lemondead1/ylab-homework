package com.lemondead1.carshopservice;

import com.lemondead1.carshopservice.service.SessionService;
import jakarta.servlet.ServletSecurityElement;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServlet;
import org.eclipse.jetty.ee10.webapp.WebAppContext;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.util.resource.ResourceFactory;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class JettyInitializer {
  private final WebAppContext context;

  public JettyInitializer(SessionService sessionService) {
    context = new WebAppContext();

    var resourceFactory = ResourceFactory.of(context);
    var webResource = resourceFactory.newClassLoaderResource("web.xml");

    context.setBaseResource(webResource);
    context.setContextPath("/");
    context.setParentLoaderPriority(true);

    var errorHandler = new ErrorHandler();
    errorHandler.setShowCauses(false);
    errorHandler.setShowStacks(false);
    context.setErrorHandler(errorHandler);

    context.getSecurityHandler().setAuthenticator(new BasicAuthenticator());
    context.getSecurityHandler().setLoginService(sessionService);
    context.getSecurityHandler().setRealmName("car-shop");
  }

  public Server createJetty(int port) {
    var server = new Server(port);
    server.setHandler(context);
    return server;
  }

  public void registerServlet(HttpServlet servlet) {
    //Manually loading annotations because I want to inject dependencies into servlets.
    var dynamic = context.getServletContext().addServlet(servlet.getClass().getName(), servlet);
    var webServlet = servlet.getClass().getAnnotation(WebServlet.class);
    Objects.requireNonNull(webServlet, "WebServlet annotation is required.");
    dynamic.addMapping(webServlet.value());
    dynamic.setAsyncSupported(webServlet.asyncSupported());
    dynamic.setLoadOnStartup(webServlet.loadOnStartup());
    dynamic.setInitParameters(Arrays.stream(webServlet.initParams())
                                    .collect(Collectors.toMap(WebInitParam::name, WebInitParam::value)));
    if (servlet.getClass().isAnnotationPresent(ServletSecurity.class)) {
      context.setServletSecurity(dynamic,
                                 new ServletSecurityElement(servlet.getClass().getAnnotation(ServletSecurity.class)));
    }
  }

  public void registerFilter(HttpFilter filter, boolean matchAfter) {
    var dynamic = context.getServletContext().addFilter(filter.getClass().getName(), filter);
    var webFilter = filter.getClass().getAnnotation(WebFilter.class);
    Objects.requireNonNull(webFilter, "WebFilter annotation is required.");
    dynamic.setInitParameters(Arrays.stream(webFilter.initParams())
                                    .collect(Collectors.toMap(WebInitParam::name, WebInitParam::value)));
    dynamic.addMappingForUrlPatterns(EnumSet.copyOf(List.of(webFilter.dispatcherTypes())),
                                     matchAfter,
                                     webFilter.value());
  }
}
