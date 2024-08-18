package com.lemondead1.carshopservice.filter;

import com.lemondead1.carshopservice.entity.User;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.annotation.Nullable;
import java.io.IOException;

@WebFilter(value = "/*", asyncSupported = true, dispatcherTypes = DispatcherType.REQUEST)
public class RequestCaptorFilter extends HttpFilter {
  private final ThreadLocal<HttpServletRequest> requests = new ThreadLocal<>();

  @Nullable
  public User getCurrentPrincipal() {
    var request = requests.get();
    if (request == null) {
      throw new IllegalStateException("No request is available.");
    }
    return (User) request.getUserPrincipal();
  }

  @Override
  protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
    requests.set(req);
    try {
      chain.doFilter(req, res);
    } finally {
      requests.remove();
    }
  }
}
