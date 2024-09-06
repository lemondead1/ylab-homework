package com.lemondead1.carshopservice.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lemondead1.carshopservice.exceptions.BadRequestException;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter(value = "/*", asyncSupported = true, dispatcherTypes = DispatcherType.REQUEST)
public class ExceptionTranslatorFilter extends HttpFilter {
  @Override
  protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
    try {
      super.doFilter(req, res, chain);
    } catch (JsonProcessingException e) {
      throw new BadRequestException(e.getOriginalMessage());
    }
  }
}
