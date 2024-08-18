package com.lemondead1.carshopservice;

import com.lemondead1.carshopservice.aspect.TransactionalAspect;
import com.lemondead1.carshopservice.repo.CarRepo;
import com.lemondead1.carshopservice.repo.EventRepo;
import com.lemondead1.carshopservice.repo.OrderRepo;
import com.lemondead1.carshopservice.repo.UserRepo;
import org.aspectj.lang.Aspects;
import org.testcontainers.containers.PostgreSQLContainer;

public class DBConnector {
  private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres");

  static {
    postgres.start();
  }

  public static final TestDBManager DB_MANAGER = new TestDBManager(postgres.getJdbcUrl(),
                                                                   postgres.getUsername(),
                                                                   postgres.getPassword(),
                                                                   "data",
                                                                   "infra",
                                                                   "db/changelog/test-changelog.yaml");

  static {
    DB_MANAGER.prepareForTests();
    Aspects.aspectOf(TransactionalAspect.class).setDbManager(DB_MANAGER);
  }

  public static final CarRepo CAR_REPO = new CarRepo(DB_MANAGER);
  public static final UserRepo USER_REPO = new UserRepo(DB_MANAGER);
  public static final OrderRepo ORDER_REPO = new OrderRepo(DB_MANAGER);
  public static final EventRepo EVENT_REPO = new EventRepo(DB_MANAGER, ObjectMapperHolder.jackson);
}
