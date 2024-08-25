package com.lemondead1.carshopservice.util;

import com.lemondead1.carshopservice.enums.UserRole;
import org.eclipse.jetty.ee10.servlet.security.ConstraintMapping;
import org.eclipse.jetty.ee10.servlet.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.Constraint;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class ConstraintSecurityHandlerBuilder {
  private final ConstraintSecurityHandler security = new ConstraintSecurityHandler();

  public ConstraintSecurityHandlerBuilder(LoginService loginService) {
    security.setLoginService(loginService);
    security.setAuthenticator(new BasicAuthenticator());
  }

  private String[] currentMatchers;
  private String[] currentMethods;

  public ConstraintSecurityHandlerBuilder at(String... matchers) {
    currentMatchers = matchers;
    return this;
  }

  public ConstraintSecurityHandlerBuilder with(String... methods) {
    currentMethods = methods;
    return this;
  }

  public ConstraintSecurityHandlerBuilder permit(UserRole... roles) {
    return addConstraint(Constraint.from(Arrays.stream(roles).map(UserRole::getId).toArray(String[]::new)));
  }

  public ConstraintSecurityHandlerBuilder permitAll() {
    return addConstraint(Constraint.ALLOWED);
  }

  @Nonnull
  private ConstraintSecurityHandlerBuilder addConstraint(Constraint allowed) {
    for (var matcher : currentMatchers) {
      for (var method : currentMethods) {
        var mapping = new ConstraintMapping();
        mapping.setPathSpec(matcher);
        mapping.setMethod(method);
        mapping.setConstraint(allowed);
        security.addConstraintMapping(mapping);
      }
    }
    return this;
  }

  public ConstraintSecurityHandler build() {
    return security;
  }
}
