package com.lemondead1.carshopservice.servlet;

import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.util.MapStruct;
import com.lemondead1.carshopservice.util.MapStructImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.org.checkerframework.checker.nullness.qual.Nullable;

import java.io.*;
import java.util.Map;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ServletTest {
  protected final MapStruct mapStruct = new MapStructImpl();

  @Mock
  protected HttpServletRequest request;

  @Mock
  protected HttpServletResponse response;

  protected StringWriter responseBody;

  @BeforeEach
  void setupRequestBody() {
    responseBody = new StringWriter();
  }

  protected void mockReqResp(@Nullable String path,
                             boolean awaitsResponse,
                             @Nullable String requestBody,
                             @Nullable User principal,
                             Map<String, String> params)
      throws IOException {
    params.forEach((k, v) -> when(request.getParameter(k)).thenReturn(v));
    if (path != null) {
      when(request.getPathInfo()).thenReturn(path);
    }
    if (requestBody != null) {
      when(request.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));
    }
    if (principal != null) {
      when(request.getUserPrincipal()).thenReturn(principal);
    }
    if (awaitsResponse) {
      when(response.getWriter()).thenReturn(new PrintWriter(responseBody));
    }
  }
}
