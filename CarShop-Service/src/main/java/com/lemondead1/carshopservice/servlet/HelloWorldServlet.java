package com.lemondead1.carshopservice.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.http.HttpHeader;

import java.io.IOException;

@RequiredArgsConstructor
public class HelloWorldServlet extends HttpServlet {
  private final ObjectMapper objectMapper;

  public record HelloMessage(String message) { }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.setHeader(HttpHeader.CONTENT_TYPE.asString(), "application/json");
    try (var writer = resp.getWriter()) {
      objectMapper.writeValue(writer, new HelloMessage("Hello world!"));
    }
  }
}
