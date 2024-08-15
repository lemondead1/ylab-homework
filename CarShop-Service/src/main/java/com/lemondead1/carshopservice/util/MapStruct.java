package com.lemondead1.carshopservice.util;

import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.security.CustomUserPrincipal;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface MapStruct {
  CustomUserPrincipal userToCustomUserPrincipal(User user);
}
