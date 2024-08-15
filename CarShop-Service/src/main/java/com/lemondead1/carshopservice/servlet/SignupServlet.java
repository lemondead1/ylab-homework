package com.lemondead1.carshopservice.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemondead1.carshopservice.dto.ResponseDTO;
import com.lemondead1.carshopservice.dto.SignupDTO;
import com.lemondead1.carshopservice.service.SessionService;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;

@RequiredArgsConstructor
public class SignupServlet extends HttpServlet {
  private final ObjectMapper objectMapper;
  private final SessionService sessionService;

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    var signup = objectMapper.readValue(req.getInputStream(), SignupDTO.class);
    signup.validate();
    sessionService.signUserUp(signup.username(), signup.phoneNumber(), signup.username(), signup.password());

    resp.setContentType("application/json");
    resp.setStatus(HttpStatus.CREATED_201);
    var responseMessage = new ResponseDTO(HttpStatus.CREATED_201, "Registered a new user.");
    objectMapper.writeValue(resp.getWriter(), responseMessage);
  }
}
