package com.lemondead1.carshopservice.aspect;

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
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Component
@Aspect
@Setter
public class AuditedAspect {
  private EventService eventService;
  private Supplier<User> currentUserProvider;

  private final Map<Method, AuditedMethod> auditedMethodCache = new ConcurrentHashMap<>();

  @Pointcut("execution(@com.lemondead1.carshopservice.annotations.Audited * * (..))")
  public void annotatedByAudited() { }

  @AfterReturning("annotatedByAudited()")
  public void afterReturning(JoinPoint jp) {
    User currentUser = currentUserProvider.get();
    if (currentUser == null) {
      throw new IllegalStateException("No user is currently authenticated.");
    }

    var method = ((MethodSignature) jp.getSignature()).getMethod();
    AuditedMethod audited = auditedMethodCache.computeIfAbsent(method, this::preprocessMethod);
    EventType type = audited.type;
    Map<String, Object> arguments = new LinkedHashMap<>();
    for (int i = 0; i < audited.auditedArgumentCount; i++) {
      Object arg = jp.getArgs()[audited.argIndices[i]];
      if (arg == null) {
        continue;
      }
      if (audited.onlyPresence[i]) {
        arguments.put(audited.argNames[i], true);
      } else {
        arguments.put(audited.argNames[i], arg);
      }
    }
    eventService.postEvent(currentUser.id(), type, arguments);
  }

  private AuditedMethod preprocessMethod(Method method) {
    var annotation = method.getAnnotation(Audited.class);
    EventType type = annotation.value();

    List<String> argNames = new ArrayList<>();
    List<Integer> argIndices = new ArrayList<>();
    List<Boolean> onlyPresence = new ArrayList<>();
    for (int i = 0; i < method.getParameterCount(); i++) {
      var parameter = method.getParameters()[i];

      if (parameter.isAnnotationPresent(Audited.Param.class)) {
        var name = parameter.getAnnotation(Audited.Param.class).value();
        argIndices.add(i);
        argNames.add(name);
        onlyPresence.add(false);
      } else if (parameter.isAnnotationPresent(Audited.PresenceCheck.class)) {
        var name = parameter.getAnnotation(Audited.PresenceCheck.class).value();
        argIndices.add(i);
        argNames.add(name);
        onlyPresence.add(true);
      }
    }

    boolean[] onlyPresenceArr = new boolean[onlyPresence.size()];
    for (int i = 0; i < onlyPresenceArr.length; i++) {
      onlyPresenceArr[i] = onlyPresence.get(i);
    }
    return new AuditedMethod(type,
                             argIndices.size(),
                             argNames.toArray(new String[0]),
                             argIndices.stream().mapToInt(Integer::intValue).toArray(),
                             onlyPresenceArr);
  }

  private record AuditedMethod(EventType type,
                               int auditedArgumentCount,
                               String[] argNames,
                               int[] argIndices,
                               boolean[] onlyPresence) { }
}
