package com.lemondead1.audit.aspect;

import com.lemondead1.audit.Auditor;
import com.lemondead1.audit.annotations.Audited;
import jakarta.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import java.util.Map;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AuditedAspectTest {
  @Mock
  Auditor eventService;

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
    proxy.testMethod("a", null, "c", "d");

    verify(eventService).postEvent("something",
                                   Map.of("first_param", "a", "second_param", "c", "presence_check_two", true));
  }

  public interface TestInterface {
    void testMethod(String value, @Nullable String value1, String value2, @Nullable String value3);
  }

  public static class TestClass implements TestInterface {
    @Audited("something")
    @Override
    public void testMethod(@Audited.Param("first_param") String value,
                           @Audited.PresenceCheck("presence_check") @Nullable String value1,
                           @Audited.Param("second_param") String value2,
                           @Audited.PresenceCheck("presence_check_two") @Nullable String value3) { }
  }
}
