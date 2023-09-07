package br.psi.giganet.api.purchase.integration.utils;

public interface RolesIntegrationTest {

    void readAuthorized() throws Exception;

    void writeAuthorized() throws Exception;

    void readUnauthorized() throws Exception;

    void writeUnauthorized() throws Exception;

}
