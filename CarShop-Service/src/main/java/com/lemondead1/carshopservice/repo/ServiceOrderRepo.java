package com.lemondead1.carshopservice.repo;

import com.lemondead1.carshopservice.enums.PurchaseOrderState;
import com.lemondead1.carshopservice.exceptions.RowNotFoundException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceOrderRepo {
  public record ServiceOrder(int id, Instant createdAt, PurchaseOrderState state, int customerId, int carId,
                             String complaints) { }

  private final Map<Integer, ServiceOrder> map = new HashMap<>();
  private int lastId = 0;

  public int create(Instant createdAt, PurchaseOrderState state, int customerId, int carId, String complaints) {
    lastId++;
    map.put(lastId, new ServiceOrder(lastId, createdAt, state, customerId, carId, complaints));
    return lastId;
  }

  public void edit(int id, Instant newCreatedAt, PurchaseOrderState newState, int newCustomerId, int newCarId,
                   String complaints) {
    if (!map.containsKey(id)) {
      throw new RowNotFoundException();
    }
    map.put(id, new ServiceOrder(id, newCreatedAt, newState, newCustomerId, newCarId, complaints));
  }

  public ServiceOrder delete(int id) {
    var old = map.remove(id);
    if (old == null) {
      throw new RowNotFoundException();
    }
    return old;
  }

  public List<ServiceOrder> listAllOrders() {
    return new ArrayList<>(map.values());
  }
}
