package com.lemondead1.carshopservice.repo;

import com.lemondead1.carshopservice.enums.PurchaseOrderState;
import com.lemondead1.carshopservice.exceptions.RowNotFoundException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PurchaseOrderRepo {
  public record PurchaseOrder(int id, Instant createdAt, PurchaseOrderState state, int customerId, int carId) { }

  private final Map<Integer, PurchaseOrder> map = new HashMap<>();
  private int lastId = 0;

  public int create(Instant createdAt, PurchaseOrderState state, int customerId, int carId) {
    lastId++;
    map.put(lastId, new PurchaseOrder(lastId, createdAt, state, customerId, carId));
    return lastId;
  }

  public void edit(int id, Instant newCreatedAt, PurchaseOrderState newState, int newCustomerId, int newCarId) {
    if (!map.containsKey(id)) {
      throw new RowNotFoundException();
    }
    map.put(id, new PurchaseOrder(id, newCreatedAt, newState, newCustomerId, newCarId));
  }

  public PurchaseOrder delete(int id) {
    var old = map.remove(id);
    if (old == null) {
      throw new RowNotFoundException();
    }
    return old;
  }

  public List<PurchaseOrder> listAllOrders() {
    return new ArrayList<>(map.values());
  }
}
