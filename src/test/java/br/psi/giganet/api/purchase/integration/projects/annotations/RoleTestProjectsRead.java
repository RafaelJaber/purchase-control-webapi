package br.psi.giganet.api.purchase.integration.projects.annotations;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@WithMockUser(username = "teste@teste.com", authorities = {"ROLE_PROJECTS_READ"})
public @interface RoleTestProjectsRead {
}
