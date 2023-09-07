package br.psi.giganet.api.purchase.integration.commons.notifications.annotations;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@WithMockUser(username = "teste@teste.com", authorities = {
        "ROLE_NOTIFICATIONS",
        "ROLE_NOTIFICATIONS_NEW_QUOTATION_APPROVAL",
        "ROLE_NOTIFICATIONS_EVALUATE_QUOTATION_APPROVAL",
        "ROLE_NOTIFICATIONS_EVALUATE_PURCHASE_REQUEST_APPROVAL",
        "ROLE_NOTIFICATIONS_RECEIVE_PURCHASE_ORDER_ITEMS"
})
public @interface RoleTestNotificationsRead {
}
