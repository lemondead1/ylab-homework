package com.lemondead1.carshopservice.aspect;

import com.lemondead1.carshopservice.filter.RequestCaptorFilter;
import com.lemondead1.carshopservice.annotations.Audited;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.service.EventService;
import lombok.Setter;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
public class AuditedAspect {
  @Setter
  private static EventService eventService;

  private final Map<Method, AuditedMethod> auditedMethodCache = new ConcurrentHashMap<>();

  @Pointcut("execution(@com.lemondead1.carshopservice.annotations.Audited * * (..))")
  public void annotatedByAudited() { }

  @AfterReturning("annotatedByAudited()")
  public void afterReturning(JoinPoint jp) {
    User currentUser = RequestCaptorFilter.getCurrentPrincipal();
    if (currentUser == null) {
      throw new IllegalStateException("No user is currently authenticated.");
    }

    var method = ((MethodSignature) jp.getSignature()).getMethod();
    var audited = auditedMethodCache.computeIfAbsent(method, this::preprocessMethod);
    var type = audited.type;
    Map<String, Object> arguments = new LinkedHashMap<>();
    for (int i = 0; i < audited.auditedArgumentCount; i++) {
      var arg = jp.getArgs()[audited.argIndices[i]];
      if (arg == null) {
        continue;
      }
      arguments.put(audited.argNames[i], arg);
    }
    eventService.postEvent(currentUser.id(), type, arguments);
  }

  private AuditedMethod preprocessMethod(Method method) {
    var annotation = method.getAnnotation(Audited.class);
    var type = annotation.value();

    List<String> argNames = new ArrayList<>();
    List<Integer> argIndices = new ArrayList<>();
    for (int i = 0; i < method.getParameterCount(); i++) {
      var parameter = method.getParameters()[i];

      if (parameter.isAnnotationPresent(Audited.Param.class)) {
        var name = parameter.getAnnotation(Audited.Param.class).value();
        argIndices.add(i);
        argNames.add(name);
      }
    }
    return new AuditedMethod(type,
                             argIndices.size(),
                             argNames.toArray(new String[0]),
                             argIndices.stream().mapToInt(Integer::intValue).toArray());
  }

  private record AuditedMethod(EventType type, int auditedArgumentCount, String[] argNames, int[] argIndices) { }
}
