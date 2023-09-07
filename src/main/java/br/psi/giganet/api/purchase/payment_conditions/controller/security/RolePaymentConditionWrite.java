package br.psi.giganet.api.purchase.payment_conditions.controller.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAnyRole({ 'ROLE_PAYMENT_CONDITIONS_WRITE', 'ROLE_ROOT' })")
public @interface RolePaymentConditionWrite {
}
