package com.lemondead1.carshopservice.aspect;

import com.lemondead1.carshopservice.database.DBManager;
import lombok.Setter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Setter
@Aspect
public class TransactionalAspect {
  private DBManager dbManager;

  @Pointcut("execution(@com.lemondead1.carshopservice.annotations.Transactional * * (..))")
  public void annotatedByTransactional() { }

  @Around("annotatedByTransactional()")
  public Object around(ProceedingJoinPoint pjp) throws Throwable {
    dbManager.startTransaction();
    try {
      var result = pjp.proceed();
      dbManager.commit();
      return result;
    } catch (Throwable e) {
      dbManager.rollback();
      throw e;
    }
  }
}
