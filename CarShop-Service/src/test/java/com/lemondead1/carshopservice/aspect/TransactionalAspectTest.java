package com.lemondead1.carshopservice.aspect;

import com.lemondead1.carshopservice.database.DBManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionalAspectTest {
  @Mock
  DBManager dbManager;

  @Mock
  ProceedingJoinPoint proceedingJoinPoint;

  TransactionalAspect transactionalAspect;

  @BeforeEach
  void beforeEach() {
    transactionalAspect = new TransactionalAspect(dbManager);
  }

  @Test
  @DisplayName("popTransaction(false) is called when the PJP finishes successfully.")
  void testTransactionIsCommitted() throws Throwable {
    transactionalAspect.around(proceedingJoinPoint);

    verify(dbManager).popTransaction(false);
  }

  @Test
  @DisplayName("popTransaction(true) is called when the PJP finishes with an exception.")
  void testTransactionIsRolledBack() throws Throwable {
    when(proceedingJoinPoint.proceed()).thenThrow(new RuntimeException());

    try {
      transactionalAspect.around(proceedingJoinPoint);
    } catch (Exception ignored) { }

    verify(dbManager).popTransaction(true);
  }
}
