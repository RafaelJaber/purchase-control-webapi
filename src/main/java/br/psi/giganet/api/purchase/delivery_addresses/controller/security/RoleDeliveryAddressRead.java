package br.psi.giganet.api.purchase.delivery_addresses.controller.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAnyRole({ 'ROLE_DELIVERY_ADDRESSES_WRITE', 'ROLE_DELIVERY_ADDRESSES_READ', 'ROLE_ROOT' })")
public @interface RoleDeliveryAddressRead {
}
