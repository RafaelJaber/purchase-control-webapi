package br.psi.giganet.api.purchase.integration.units.tests;

import br.psi.giganet.api.purchase.config.security.repository.PermissionRepository;
import br.psi.giganet.api.purchase.employees.repository.EmployeeRepository;
import br.psi.giganet.api.purchase.integration.units.annotations.RoleTestUnitsRead;
import br.psi.giganet.api.purchase.integration.units.annotations.RoleTestUnitsWrite;
import br.psi.giganet.api.purchase.integration.utils.BuilderIntegrationTest;
import br.psi.giganet.api.purchase.integration.utils.RolesIntegrationTest;
import br.psi.giganet.api.purchase.integration.utils.annotations.RoleTestAdmin;
import br.psi.giganet.api.purchase.integration.utils.annotations.RoleTestRoot;
import br.psi.giganet.api.purchase.units.controller.request.UnitRequest;
import br.psi.giganet.api.purchase.units.model.Unit;
import br.psi.giganet.api.purchase.units.repository.UnitRepository;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import javax.transaction.Transactional;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UnitsTest extends BuilderIntegrationTest implements RolesIntegrationTest {

    private Unit unitTest;

    @Autowired
    public UnitsTest(
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository,
            UnitRepository unitRepository) {

        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;
        this.unitRepository = unitRepository;
        createCurrentUser();

        unitTest = createAndSaveUnit();
    }

    @RoleTestRoot
    public void findAll() throws Exception {
        this.mockMvc.perform(get("/units")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(fieldWithPath("[]").description("Lista de unidades encontradas"))
                                .andWithPrefix("[].",
                                        fieldWithPath("id").description("Código da unidade"),
                                        fieldWithPath("name").description("Nome"),
                                        fieldWithPath("abbreviation").description("Abreviação da unidade"))
                ));
    }

    @RoleTestRoot
    public void findById() throws Exception {
        this.mockMvc.perform(get("/units/{id}", unitTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código da unidade buscada")
                        ),
                        getUnitResponse()));
    }

    @RoleTestRoot
    @Transactional
    public void insert() throws Exception {
        this.mockMvc.perform(post("/units")
                .content(new ObjectMapper().writeValueAsString(createUnitRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("name").description(
                                        createDescriptionWithNotEmpty("Nome")),
                                fieldWithPath("abbreviation").description(
                                        createDescriptionWithNotEmpty("Abreviação da unidade")),
                                fieldWithPath("description")
                                        .optional()
                                        .type(JsonFieldType.STRING)
                                        .description(createDescription("Descrição da unidade")),
                                fieldWithPath("conversions")
                                        .optional()
                                        .type(JsonFieldType.ARRAY)
                                        .description(createDescription("Lista de conversões associadas")))
                                .andWithPrefix("conversions[].",
                                        fieldWithPath("id").type(JsonFieldType.NUMBER)
                                                .optional()
                                                .description("Código do registro"),
                                        fieldWithPath("to").type(JsonFieldType.NUMBER)
                                                .optional()
                                                .description("Código da unidade de destino"),
                                        fieldWithPath("conversion").type(JsonFieldType.NUMBER)
                                                .optional()
                                                .description("Fator de conversão da presente unidade para a unidade destino.")),
                        getUnitResponse()));
    }

    @RoleTestRoot
    @Transactional
    public void update() throws Exception {
        this.mockMvc.perform(put("/units/{id}", unitTest.getId())
                .content(new ObjectMapper().writeValueAsString(createUnitRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código da categoria a ser atualizada")
                        ),
                        requestFields(
                                fieldWithPath("name").description(
                                        createDescriptionWithNotEmpty("Nome")),
                                fieldWithPath("abbreviation").description(
                                        createDescriptionWithNotEmpty("Abreviação da unidade")),
                                fieldWithPath("description")
                                        .optional()
                                        .type(JsonFieldType.STRING)
                                        .description(createDescription("Descrição da unidade")),
                                fieldWithPath("conversions")
                                        .optional()
                                        .type(JsonFieldType.ARRAY)
                                        .description(createDescription("Lista de conversões associadas")))
                                .andWithPrefix("conversions[].",
                                        fieldWithPath("id").type(JsonFieldType.NUMBER)
                                                .optional()
                                                .description("Código do registro"),
                                        fieldWithPath("to").type(JsonFieldType.NUMBER)
                                                .optional()
                                                .description("Código da unidade de destino"),
                                        fieldWithPath("conversion").type(JsonFieldType.NUMBER)
                                                .optional()
                                                .description("Fator de conversão da presente unidade para a unidade destino.")),
                        getUnitResponse()));
    }

    @Override
    @RoleTestUnitsRead
    public void readAuthorized() throws Exception {
        this.mockMvc.perform(get("/units"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/units/{id}", unitTest.getId()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    @RoleTestAdmin
    public void readUnauthorized() throws Exception {
        this.mockMvc.perform(get("/units"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/units/{id}", unitTest.getId()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @Override
    @RoleTestUnitsWrite
    public void writeAuthorized() throws Exception {
        this.mockMvc.perform(post("/units")
                .content(new ObjectMapper().writeValueAsString(createUnitRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated());

        this.mockMvc.perform(put("/units/{id}", unitTest.getId())
                .content(new ObjectMapper().writeValueAsString(createUnitRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    @RoleTestAdmin
    public void writeUnauthorized() throws Exception {
        this.mockMvc.perform(post("/units")
                .content(new ObjectMapper().writeValueAsString(createUnitRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(put("/units/{id}", unitTest.getId())
                .content(new ObjectMapper().writeValueAsString(createUnitRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    private ResponseFieldsSnippet getUnitResponse() {
        return responseFields(
                fieldWithPath("id").description("Código da unidade"),
                fieldWithPath("name").description("Nome"),
                fieldWithPath("abbreviation").description("Abreviação da unidade"),
                fieldWithPath("description").description("Descrição da unidade"),
                fieldWithPath("conversions")
                        .optional()
                        .type(JsonFieldType.ARRAY)
                        .description("Lista com todas as conversões cadastradas para esta unidade"))
                .andWithPrefix("conversions[].",
                        fieldWithPath("id").type(JsonFieldType.NUMBER)
                                .optional()
                                .description("Código do registro"),
                        fieldWithPath("to").type(JsonFieldType.OBJECT)
                                .optional()
                                .description("Unidade de destino"),
                        fieldWithPath("conversion").type(JsonFieldType.NUMBER)
                                .optional()
                                .description("Fator de conversão da presente unidade para a unidade destino."))
                .andWithPrefix("conversions[].to.",
                        fieldWithPath("id").type(JsonFieldType.NUMBER)
                                .optional()
                                .description("Código da unidade"),
                        fieldWithPath("name").type(JsonFieldType.STRING)
                                .optional()
                                .description("Nome da unidade"),
                        fieldWithPath("abbreviation").type(JsonFieldType.STRING)
                                .optional()
                                .description("Abreviação utilizada para a unidade"));
    }

    private UnitRequest createUnitRequest() {
        UnitRequest request = new UnitRequest();
        request.setName("Categoria");
        request.setDescription("Descrição de teste");
        request.setAbbreviation("cat");
        return request;
    }

}
