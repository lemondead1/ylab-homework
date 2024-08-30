package com.lemondead1.carshopservice.aspect;

import com.lemondead1.carshopservice.annotations.Audited;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.service.EventService;
import jakarta.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.Map;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AuditedAspectTest {
  @Mock
  EventService eventService;

  TestInterface proxy;

  @BeforeEach
  void beforeEach() {
    var proxyFactory = new AspectJProxyFactory(new TestClass());
    proxyFactory.addAspect(new AuditedAspect(eventService));
    proxy = proxyFactory.getProxy();
  }

  @Test
  @DisplayName("EventService.postEvent is called when testMethod is executed.")
  void testAuditedAspect() {
    var currentRequest = new MockHttpServletRequest();
    currentRequest.setUserPrincipal(new User(1, "admin", "88005553535", "admin@ya.com", "password", UserRole.ADMIN, 0));
    RequestContextHolder.setRequestAttributes(new ServletWebRequest(currentRequest));

    proxy.testMethod("a", null, "c", "d");

    verify(eventService).postEvent(1, EventType.CAR_CREATED,
                                   Map.of("first_param", "a", "second_param", "c", "presence_check_two", true));
  }

  public interface TestInterface {
    void testMethod(String value, @Nullable String value1, String value2, @Nullable String value3);
  }

  public static class TestClass implements TestInterface {
    @Override
    @Audited(EventType.CAR_CREATED)
    public void testMethod(@Audited.Param("first_param") String value,
                           @Audited.PresenceCheck("presence_check") @Nullable String value1,
                           @Audited.Param("second_param") String value2,
                           @Audited.PresenceCheck("presence_check_two") @Nullable String value3) { }
  }
}
