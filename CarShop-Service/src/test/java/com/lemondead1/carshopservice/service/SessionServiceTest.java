package com.lemondead1.carshopservice.service;


import com.lemondead1.carshopservice.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SessionServiceTest {
  @Mock
  UserService users;

  SessionService session;

  @BeforeEach
  void setup() {
    session = new SessionService(users);
  }

  @Test
  void getCurrentUserRoleReturnsNonAnonymousWhenIdIsNot0() {
    when(users.getUserRole(123)).thenReturn(UserRole.ADMIN);

    session.setCurrentUserId(123);

    assertThat(session.getCurrentUserRole()).isEqualTo(UserRole.ADMIN);

    verify(users).getUserRole(123);
  }

  @Test
  void getCurrentUserRoleReturnsAnonymousWhenIdIs0() {
    session.setCurrentUserId(0);

    assertThat(session.getCurrentUserRole()).isEqualTo(UserRole.ANONYMOUS);

    verifyNoInteractions(users);
  }
}
