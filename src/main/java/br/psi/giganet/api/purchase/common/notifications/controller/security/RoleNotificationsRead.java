package br.psi.giganet.api.purchase.common.notifications.controller.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAnyRole({ 'ROLE_NOTIFICATIONS', 'ROLE_NOTIFICATIONS_NEW_QUOTATION_APPROVAL'," +
        "'ROLE_NOTIFICATIONS_EVALUATE_QUOTATION_APPROVAL', 'ROLE_NOTIFICATIONS_EVALUATE_PURCHASE_REQUEST_APPROVAL'," +
        "'ROLE_NOTIFICATIONS_RECEIVE_PURCHASE_ORDER_ITEMS', 'ROLE_ROOT' })")
public @interface RoleNotificationsRead {
}
