package com.lemondead1.carshopservice.aspect;

import com.lemondead1.carshopservice.database.DBManager;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Component
@Aspect
@RequiredArgsConstructor
public class TransactionalAspect {
  private final DBManager dbManager;

  @Pointcut("execution(@com.lemondead1.carshopservice.annotations.Transactional * * (..))")
  public void annotatedByTransactional() { }

  @Around("annotatedByTransactional()")
  public Object around(ProceedingJoinPoint pjp) throws Throwable {
    dbManager.pushTransaction();
    try {
      Object result = pjp.proceed();
      dbManager.popTransaction(false);
      return result;
    } catch (Throwable e) {
      dbManager.popTransaction(true);
      throw e;
    }
  }
}
