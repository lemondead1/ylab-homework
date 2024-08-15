package com.lemondead1.carshopservice.aspect;

import com.lemondead1.carshopservice.annotations.Audited;
import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.service.EventService;
import lombok.Setter;
import org.aspectj.lang.ProceedingJoinPoint;
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
@Setter
public class AuditedAspect {
  private static EventService eventService;

  private final Map<Method, AuditedMethod> auditedMethodCache = new ConcurrentHashMap<>();

  @Pointcut("execution(@com.lemondead1.carshopservice.annotations.Audited * * (..))")
  public void annotatedByAudited() { }

  @AfterReturning("annotatedByAudited()")
  public void afterReturning(ProceedingJoinPoint pjp) {
    var method = ((MethodSignature) pjp.getSignature()).getMethod();

    var audited = auditedMethodCache.computeIfAbsent(method, this::preprocessMethod);
    int userId = (int) pjp.getArgs()[audited.userIdIndex];
    var type = audited.type;
    Map<String, Object> arguments = new LinkedHashMap<>();
    for (int i = 0; i < audited.userIdIndex; i++) {
      arguments.put(audited.argNames[i], pjp.getArgs()[audited.argIndices[i]]);
    }
    
    eventService.postEvent(userId, type, arguments);
  }

  private AuditedMethod preprocessMethod(Method method) {
    var annotation = method.getAnnotation(Audited.class);
    var type = annotation.value();

    List<String> argNames = new ArrayList<>();
    List<Integer> argIndices = new ArrayList<>();
    int userIdIndex = 0;
    boolean userIdIndexSet = false;
    for (int i = 0; i < method.getParameterCount(); i++) {
      var parameter = method.getParameters()[i];

      if (parameter.isAnnotationPresent(Audited.UserId.class)) {
        if (userIdIndexSet) {
          throw new IllegalArgumentException("Encountered duplicate user id.");
        }
        userIdIndex = i;
        userIdIndexSet = true;
      } else if (parameter.isAnnotationPresent(Audited.Param.class)) {
        var name = parameter.getAnnotation(Audited.Param.class).value();
        argIndices.add(i);
        argNames.add(name);
      }
    }
    if (!userIdIndexSet) {
      throw new IllegalArgumentException("Could not find user id parameter.");
    }
    return new AuditedMethod(type,
                             argIndices.size(),
                             argNames.toArray(new String[0]),
                             argIndices.stream().mapToInt(Integer::intValue).toArray(),
                             userIdIndex);
  }

  private record AuditedMethod(EventType type,
                               int auditedArgumentCount, String[] argNames, int[] argIndices,
                               int userIdIndex) { }
}
