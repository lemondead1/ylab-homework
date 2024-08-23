package com.lemondead1.carshopservice.util;

import com.lemondead1.carshopservice.dto.car.ExistingCarDTO;
import com.lemondead1.carshopservice.dto.event.EventDTO;
import com.lemondead1.carshopservice.dto.order.ExistingOrderDTO;
import com.lemondead1.carshopservice.dto.user.ExistingUserDTO;
import com.lemondead1.carshopservice.entity.Car;
import com.lemondead1.carshopservice.entity.Event;
import com.lemondead1.carshopservice.entity.Order;
import com.lemondead1.carshopservice.entity.User;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR, componentModel = "spring")
public interface MapStruct {
  //Uhh
  @Mapping(expression = "java(user.id())", target = "id")
  @Mapping(expression = "java(user.username())", target = "username")
  @Mapping(expression = "java(user.phoneNumber())", target = "phoneNumber")
  @Mapping(expression = "java(user.email())", target = "email")
  @Mapping(expression = "java(user.role())", target = "role")
  @Mapping(expression = "java(user.purchaseCount())", target = "purchaseCount")
  ExistingUserDTO userToUserDto(User user);

  List<ExistingUserDTO> userToUserDtoList(List<User> users);

  ExistingCarDTO carToCarDto(Car car);

  List<ExistingCarDTO> carListToDtoList(List<Car> cars);

  @Mapping(source = "type", target = "kind")
  @Mapping(source = "comments", target = "comment")
  ExistingOrderDTO orderToOrderDto(Order order);

  List<ExistingOrderDTO> orderListToDtoList(List<Order> orders);

  @Mapping(source = "json", target = "data")
  EventDTO eventToEventDto(Event event);

  List<EventDTO> eventListToDtoList(List<Event> events);
}
