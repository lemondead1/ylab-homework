package com.lemondead1.audit.aspect;

import com.lemondead1.audit.Auditor;
import com.lemondead1.audit.annotations.Audited;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class AuditedAspect {
  private final Auditor eventService;

  private final Map<Method, AuditedMethod> auditedMethodCache = new ConcurrentHashMap<>();

  @Pointcut("execution(@com.lemondead1.audit.annotations.Audited * * (..))")
  public void annotatedByAudited() { }

  @AfterReturning("annotatedByAudited()")
  public void afterReturning(JoinPoint jp) throws NoSuchMethodException {
    Method iFaceMethod = ((MethodSignature) jp.getSignature()).getMethod();
    Method implMethod = jp.getTarget().getClass().getMethod(iFaceMethod.getName(), iFaceMethod.getParameterTypes());

    AuditedMethod audited = auditedMethodCache.computeIfAbsent(implMethod, this::preprocessMethod);
    String type = audited.type;
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
    eventService.postEvent(type, arguments);
  }

  private AuditedMethod preprocessMethod(Method method) {
    var annotation = method.getAnnotation(Audited.class);
    String type = annotation.value();

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

  private record AuditedMethod(String type,
                               int auditedArgumentCount,
                               String[] argNames,
                               int[] argIndices,
                               boolean[] onlyPresence) { }
}
