package br.psi.giganet.api.purchase.integration.commons;

import br.psi.giganet.api.purchase.config.exception.exception.UnauthenticatedException;
import br.psi.giganet.api.purchase.integration.utils.BuilderIntegrationTest;
import br.psi.giganet.api.purchase.integration.utils.annotations.RoleTestAdmin;
import br.psi.giganet.api.purchase.products.controller.ProductController;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ErrorHandlerTest extends BuilderIntegrationTest {

    @MockBean
    private ProductController controller;

    @Test
    public void notFound() throws Exception {
        this.mockMvc.perform(get("/employees/9999"))
                .andExpect(status().isNotFound())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                responseFields(fieldWithPath("error").description("Descrição do erro encontrado"))));
    }

    @Test
    public void badRequest() throws Exception {
        this.mockMvc.perform(get("/products/abc"))
                .andExpect(status().isBadRequest())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint())));
    }

    @RoleTestAdmin
    public void forbidden() throws Exception {
        this.mockMvc.perform(get("/units"))
                .andExpect(status().isForbidden())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint())));
    }

    @Test()
    public void internalServerError() throws Exception {
        Mockito.when(controller.findById(1L)).thenThrow(new RuntimeException("Tested error"));
        this.mockMvc.perform(get("/products/1"))
                .andExpect(status().isInternalServerError())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                responseFields(fieldWithPath("error").description("Descrição do erro encontrado"))));
    }

    @Test
    public void unauthenticated() throws Exception {
        Mockito.when(controller.findById(2L)).thenThrow(UnauthenticatedException.class);
        this.mockMvc.perform(get("/products/2"))
                .andExpect(status().isUnauthorized())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                responseFields(fieldWithPath("error").description("Descrição do erro encontrado"))));
    }

}
