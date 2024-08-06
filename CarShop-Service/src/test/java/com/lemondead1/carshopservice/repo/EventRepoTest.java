package com.lemondead1.carshopservice.repo;

import org.junit.jupiter.api.BeforeEach;

public class EventRepoTest {
  CarRepo cars;
  UserRepo users;
  OrderRepo orders;
  EventRepo events;

  @BeforeEach
  void beforeEach() {
    cars = new CarRepo();
    users = new UserRepo();
    orders = new OrderRepo();
    cars.setOrders(orders);
    users.setOrders(orders);
    orders.setCars(cars);
    orders.setUsers(users);
    events.setUsers(users);
  }
}
