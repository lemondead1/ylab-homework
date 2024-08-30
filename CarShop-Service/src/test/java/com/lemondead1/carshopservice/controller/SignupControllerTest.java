package com.lemondead1.carshopservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.service.SignupLoginService;
import com.lemondead1.carshopservice.util.MapStruct;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SignupControllerTest {
  @MockBean
  SignupLoginService signupLoginService;

  @Autowired
  ObjectMapper objectMapper;

  @Autowired
  MapStruct mapStruct;

  @Autowired
  MockMvc mockMvc;

  @Test
  @DisplayName("POST /signup calls SignupLoginService.signUserUp and responds with the new user.")
  void testSignup() throws Exception {
    User user = new User(1, "username", "88005553535", "test@example.com", "password", UserRole.CLIENT, 0);
    var requestBody = "{\"username\": \"username\", \"phone_number\": \"88005553535\", \"email\": \"test@example.com\", \"password\": \"password\"}";
    var expectedResponse = objectMapper.writeValueAsString(mapStruct.userToUserDto(user));

    when(signupLoginService.signUserUp("username", "88005553535", "test@example.com", "password")).thenReturn(user);

    mockMvc.perform(post("/signup").content(requestBody).contentType("application/json").accept("application/json"))
           .andDo(log())
           .andExpect(status().isCreated())
           .andExpect(content().string(expectedResponse));

    verify(signupLoginService).signUserUp("username", "88005553535", "test@example.com", "password");
  }
}
