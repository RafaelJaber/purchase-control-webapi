package br.psi.giganet.api.purchase.integration.suppliers.tests;

import br.psi.giganet.api.purchase.config.security.repository.PermissionRepository;
import br.psi.giganet.api.purchase.employees.repository.EmployeeRepository;
import br.psi.giganet.api.purchase.integration.suppliers.annotations.RoleTestSuppliersRead;
import br.psi.giganet.api.purchase.integration.suppliers.annotations.RoleTestSuppliersWrite;
import br.psi.giganet.api.purchase.integration.utils.BuilderIntegrationTest;
import br.psi.giganet.api.purchase.integration.utils.RolesIntegrationTest;
import br.psi.giganet.api.purchase.integration.utils.annotations.RoleTestAdmin;
import br.psi.giganet.api.purchase.integration.utils.annotations.RoleTestRoot;
import br.psi.giganet.api.purchase.suppliers.taxes.controller.request.TaxRequest;
import br.psi.giganet.api.purchase.suppliers.taxes.model.Tax;
import br.psi.giganet.api.purchase.suppliers.taxes.repository.TaxRepository;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import javax.transaction.Transactional;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TaxesTests extends BuilderIntegrationTest implements RolesIntegrationTest {

    private Tax taxTest;

    @Autowired
    public TaxesTests(
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository,
            TaxRepository taxRepository) {

        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;
        this.taxRepository = taxRepository;
        createCurrentUser();

        taxTest = createAndSaveTax();
    }

    @RoleTestRoot
    public void findAll() throws Exception {
        this.mockMvc.perform(get("/suppliers/taxes")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(fieldWithPath("[]").description("Lista de impostos cadastrados"))
                                .andWithPrefix("[].",
                                        fieldWithPath("id").description("Código do registro"),
                                        fieldWithPath("from").description("Estado de origem, em formato abreviado. Ex: MG"),
                                        fieldWithPath("to").description("Estado de destino, em formato abreviado. Ex: MG"),
                                        fieldWithPath("icms").description("Valor do ICMS associado, em porcentagem. Ex: 10 -> 10%"))));
    }

    @RoleTestRoot
    public void findById() throws Exception {
        this.mockMvc.perform(get("/suppliers/taxes/{id}", taxTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código do registro buscado")
                        ),
                        responseFields(
                                fieldWithPath("id").description("Código do registro"),
                                fieldWithPath("from").description("Estado de origem, em formato abreviado. Ex: MG"),
                                fieldWithPath("to").description("Estado de destino, em formato abreviado. Ex: MG"),
                                fieldWithPath("icms").description("Valor do ICMS associado, em porcentagem. Ex: 10 -> 10%"))));
    }

    @RoleTestRoot
    @Transactional
    public void update() throws Exception {
        this.mockMvc.perform(put("/suppliers/taxes/{id}", taxTest.getId())
                .content(new ObjectMapper().writeValueAsString(createTaxRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código do registro a ser atualizado")
                        ),
                        requestFields(
                                fieldWithPath("id").description(createDescriptionWithNotNull("Código do registro")),
                                fieldWithPath("from").description(
                                        createDescriptionWithNotEmpty("Estado de origem, em formato abreviado. Ex: MG")),
                                fieldWithPath("icms").description(
                                        createDescriptionWithPositiveAndNotNull("Valor do ICMS associado, em porcentagem. Ex: 10 -> 10%"))),
                        responseFields(
                                fieldWithPath("id").description("Código do registro"),
                                fieldWithPath("from").description("Estado de origem, em formato abreviado. Ex: MG"),
                                fieldWithPath("to").description("Estado de destino, em formato abreviado. Ex: MG"),
                                fieldWithPath("icms").description("Valor do ICMS associado, em porcentagem. Ex: 10 -> 10%"))));
    }

    @Override
    @RoleTestSuppliersRead
    public void readAuthorized() throws Exception {
        this.mockMvc.perform(get("/suppliers/taxes")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/suppliers/taxes/{id}", taxTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    @RoleTestSuppliersWrite
    public void writeAuthorized() throws Exception {
        this.mockMvc.perform(put("/suppliers/taxes/{id}", taxTest.getId())
                .content(new ObjectMapper().writeValueAsString(createTaxRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    @RoleTestAdmin
    public void readUnauthorized() throws Exception {
        this.mockMvc.perform(get("/suppliers/taxes")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/suppliers/taxes/{id}", taxTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @Override
    @RoleTestAdmin
    public void writeUnauthorized() throws Exception {
        this.mockMvc.perform(put("/suppliers/taxes/{id}", taxTest.getId())
                .content(new ObjectMapper().writeValueAsString(createTaxRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    private TaxRequest createTaxRequest() {
        TaxRequest request = new TaxRequest();
        request.setFrom("MG");
        request.setIcms(12f);
        request.setId(taxTest.getId());

        return request;
    }
}
