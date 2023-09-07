package br.psi.giganet.api.purchase.integration.purchase_orders.annotations;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import javax.transaction.Transactional;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@WithMockUser(username = "teste_competencies_write@teste.com", authorities = {"ROLE_PURCHASE_ORDERS_COMPETENCIES_WRITE"})
@Transactional
public @interface RoleTestPurchaseOrdersCompetenciesWrite {
}
