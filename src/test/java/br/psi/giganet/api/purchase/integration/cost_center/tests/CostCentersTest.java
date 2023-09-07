package br.psi.giganet.api.purchase.integration.cost_center.tests;

import br.psi.giganet.api.purchase.config.security.repository.PermissionRepository;
import br.psi.giganet.api.purchase.cost_center.controller.request.CostCenterRequest;
import br.psi.giganet.api.purchase.cost_center.model.CostCenter;
import br.psi.giganet.api.purchase.cost_center.repository.CostCenterRepository;
import br.psi.giganet.api.purchase.employees.repository.EmployeeRepository;
import br.psi.giganet.api.purchase.integration.cost_center.annotations.RoleTestCostCentersRead;
import br.psi.giganet.api.purchase.integration.cost_center.annotations.RoleTestCostCentersWrite;
import br.psi.giganet.api.purchase.integration.utils.BuilderIntegrationTest;
import br.psi.giganet.api.purchase.integration.utils.RolesIntegrationTest;
import br.psi.giganet.api.purchase.integration.utils.annotations.RoleTestAdmin;
import br.psi.giganet.api.purchase.integration.utils.annotations.RoleTestRoot;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import javax.transaction.Transactional;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CostCentersTest extends BuilderIntegrationTest implements RolesIntegrationTest {

    private CostCenter costCenterTest;

    @Autowired
    public CostCentersTest(
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository,
            CostCenterRepository costCenterRepository) {

        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;
        this.costCenterRepository = costCenterRepository;
        createCurrentUser();

        costCenterTest = createAndSaveCostCenter();
    }

    @RoleTestRoot
    public void findAll() throws Exception {
        this.mockMvc.perform(get("/cost-centers")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(fieldWithPath("[]").description("Lista de centro de custos encontrados"))
                                .andWithPrefix("[].",
                                        fieldWithPath("id").description("Código do centro de custo"),
                                        fieldWithPath("name").description("Nome"),
                                        fieldWithPath("description").description("Descrição do centro de custo"))));
    }

    @RoleTestRoot
    public void findById() throws Exception {
        this.mockMvc.perform(get("/cost-centers/{id}", costCenterTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código do centro de custo buscado")
                        ),
                        responseFields(
                                fieldWithPath("id").description("Código do centro de custo"),
                                fieldWithPath("name").description("Nome"),
                                fieldWithPath("description").description("Descrição do centro de custo"))));
    }

    @RoleTestRoot
    @Transactional
    public void insert() throws Exception {
        this.mockMvc.perform(post("/cost-centers")
                .content(new ObjectMapper().writeValueAsString(createCostCenterRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("name").description(
                                        createDescriptionWithNotEmpty("Nome")),
                                fieldWithPath("description")
                                        .optional()
                                        .type(JsonFieldType.STRING)
                                        .description(createDescription("Descrição do centro de custo"))
                        ),
                        responseFields(
                                fieldWithPath("id").description("Código do centro de custo"),
                                fieldWithPath("name").description("Nome"),
                                fieldWithPath("description").description("Descrição do centro de custo"))));
    }

    @RoleTestRoot
    @Transactional
    public void update() throws Exception {
        this.mockMvc.perform(put("/cost-centers/{id}", costCenterTest.getId())
                .content(new ObjectMapper().writeValueAsString(createCostCenterRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código do centro de custo a ser atualizado")
                        ),
                        requestFields(
                                fieldWithPath("name").description(
                                        createDescriptionWithNotEmpty("Nome")),
                                fieldWithPath("description")
                                        .optional()
                                        .type(JsonFieldType.STRING)
                                        .description(createDescription("Descrição do centro de custo"))
                        ),
                        responseFields(
                                fieldWithPath("id").description("Código do centro de custo"),
                                fieldWithPath("name").description("Nome"),
                                fieldWithPath("description").description("Descrição do centro de custo"))));
    }

    @Override
    @RoleTestCostCentersRead
    public void readAuthorized() throws Exception {
        this.mockMvc.perform(get("/cost-centers"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/cost-centers/{id}", costCenterTest.getId()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    @RoleTestAdmin
    public void readUnauthorized() throws Exception {
        this.mockMvc.perform(get("/cost-centers"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/cost-centers/{id}", costCenterTest.getId()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @Override
    @RoleTestCostCentersWrite
    public void writeAuthorized() throws Exception {
        this.mockMvc.perform(post("/cost-centers")
                .content(new ObjectMapper().writeValueAsString(createCostCenterRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated());

        this.mockMvc.perform(put("/cost-centers/{id}", costCenterTest.getId())
                .content(new ObjectMapper().writeValueAsString(createCostCenterRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    @RoleTestAdmin
    public void writeUnauthorized() throws Exception {
        this.mockMvc.perform(post("/cost-centers")
                .content(new ObjectMapper().writeValueAsString(createCostCenterRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(put("/cost-centers/{id}", costCenterTest.getId())
                .content(new ObjectMapper().writeValueAsString(createCostCenterRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    private CostCenterRequest createCostCenterRequest() {
        CostCenterRequest request = new CostCenterRequest();
        request.setName("Centro de custo");
        request.setDescription("Descrição de teste");
        return request;
    }

}
