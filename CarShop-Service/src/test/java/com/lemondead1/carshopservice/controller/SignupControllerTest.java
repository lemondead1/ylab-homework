package com.lemondead1.carshopservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemondead1.carshopservice.config.EnvironmentConfig;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.service.SignupLoginService;
import com.lemondead1.carshopservice.util.MapStruct;
import com.lemondead1.carshopservice.util.MapStructImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
public class SignupControllerTest {
  @Mock
  SignupLoginService signupLoginService;

  ObjectMapper objectMapper = EnvironmentConfig.objectMapper();

  MapStruct mapStruct = new MapStructImpl();

  SignupController controller;

  MockMvc mockMvc;

  @BeforeEach
  void beforeEach() {
    controller = new SignupController(signupLoginService, mapStruct);
    mockMvc = standaloneSetup(controller).setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                                         .build();
  }

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
