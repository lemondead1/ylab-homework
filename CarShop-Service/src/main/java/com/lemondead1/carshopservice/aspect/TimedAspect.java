package com.lemondead1.carshopservice.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
@Slf4j
public class TimedAspect {
  @Pointcut("execution(@com.lemondead1.carshopservice.annotations.Timed * * (..))")
  public void methodsWithTimed() { }

  @Pointcut("within(@com.lemondead1.carshopservice.annotations.Timed *) && execution(* * (..))")
  public void methodsInClassesWithTimed() { }

  @Around("methodsWithTimed() || methodsInClassesWithTimed()")
  public Object around(ProceedingJoinPoint pjp) throws Throwable {
    log.debug("Entering {}", pjp.getSignature().getName());
    long startNanoTime = System.nanoTime();
    try {
      return pjp.proceed();
    } finally {
      long diff = System.nanoTime() - startNanoTime;
      log.debug("{} took {} nanos to execute.", pjp.getSignature().getName(), diff);
    }
  }
}
