package br.psi.giganet.api.purchase.integration.employees.tests;

import br.psi.giganet.api.purchase.config.security.model.Permission;
import br.psi.giganet.api.purchase.config.security.repository.PermissionRepository;
import br.psi.giganet.api.purchase.employees.controller.request.UpdatePermissionsRequest;
import br.psi.giganet.api.purchase.employees.model.Employee;
import br.psi.giganet.api.purchase.employees.repository.EmployeeRepository;
import br.psi.giganet.api.purchase.integration.utils.BuilderIntegrationTest;
import br.psi.giganet.api.purchase.integration.utils.RolesIntegrationTest;
import br.psi.giganet.api.purchase.integration.utils.annotations.RoleTestAdmin;
import br.psi.giganet.api.purchase.integration.utils.annotations.RoleTestRoot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import javax.transaction.Transactional;
import java.util.stream.Collectors;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EmployeesTest extends BuilderIntegrationTest implements RolesIntegrationTest {

    private Employee employeeTest;

    @Autowired
    public EmployeesTest(
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository) {

        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;
        createCurrentUser();

        this.createEmployee();
    }

    @RoleTestRoot
    public void findByNameAndPermissions() throws Exception {
        this.mockMvc.perform(get("/employees/permissions/{permission}", "ROLE_ADMIN")
                .param("name", "emp")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("permission").description("Permissão a ser utilizada na busca")),
                                requestParameters(
                                        parameterWithName("name").optional().description("Nome a ser filtrado")),
                                responseFields(fieldWithPath("[]")
                                        .description("Lista de todos os funcionários"))
                                        .andWithPrefix("[].",
                                                fieldWithPath("id").description("Código do funcionário"),
                                                fieldWithPath("name").description("Nome do funcionário"))));
    }

    @RoleTestRoot
    @Transactional
    public void findByName() throws Exception {
        this.mockMvc.perform(get("/employees")
                .param("name", "emp")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestParameters(
                                        parameterWithName("name").optional().description("Nome a ser filtrado")),
                                responseFields(fieldWithPath("[]")
                                        .description("Lista de todos os funcionários"))
                                        .andWithPrefix("[].",
                                                fieldWithPath("id").description("Código do funcionário"),
                                                fieldWithPath("name").description("Nome do funcionário"),
                                                fieldWithPath("email").description("Email do funcionário"))));
    }

    @RoleTestRoot
    public void findById() throws Exception {
        this.mockMvc.perform(RestDocumentationRequestBuilders.get("/employees/{id}", employeeTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código do funcionário")),
                        getResponse()));
    }

    @RoleTestRoot
    @Transactional
    public void update() throws Exception {
        Employee employee = createAndSaveEmployee();

        this.mockMvc.perform(RestDocumentationRequestBuilders.put("/employees/{id}/permissions", employee.getId())
                .content(objectMapper.writeValueAsString(createUpdatePermissionsRequest(employee)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(parameterWithName("id").description("Código do funcionário")),
                        requestFields(
                                fieldWithPath("id").description("Código do funcionário"),
                                fieldWithPath("name").optional().type(JsonFieldType.STRING).description("Nome do funcionário"),
                                fieldWithPath("permissions").optional().type(JsonFieldType.ARRAY).description("Lista com as permissões do usuário")),
                        responseFields(getProjection())));
    }

    private UpdatePermissionsRequest createUpdatePermissionsRequest(Employee employee) {
        UpdatePermissionsRequest request = new UpdatePermissionsRequest();
        request.setId(employee.getId());
        request.setName(employee.getName());
        request.setPermissions(
                employee.getPermissions().stream()
                        .limit(1)
                        .map(Permission::getName)
                        .collect(Collectors.toSet()));

        request.getPermissions().add(createAndSavePermission("ROLE_PERMISSION_TEST").getName());

        return request;
    }

    private FieldDescriptor[] getProjection() {
        return new FieldDescriptor[]{
                fieldWithPath("id").description("Código do funcionário"),
                fieldWithPath("name").description("Nome")};
    }

    private ResponseFieldsSnippet getResponse() {
        return responseFields(
                fieldWithPath("id").description("Código do funcionário"),
                fieldWithPath("name").description("Nome"),
                fieldWithPath("email").description("Email"),
                fieldWithPath("createdDate").description(createDescription("Data de criação do registro",
                        "Pode ser através do primeiro login ou através da importação do funcionário")),
                fieldWithPath("lastModifiedDate").description("Data da última modificação"),
                fieldWithPath("permissions").description("Permissões associadas ao funcionário"));
    }

    @Override
    @RoleTestAdmin
    public void readAuthorized() throws Exception {
        this.mockMvc.perform(get("/employees/permissions/{permission}", "ROLE_ADMIN")
                .param("name", "emp")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/employees")
                .param("name", "emp")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        this.mockMvc.perform(RestDocumentationRequestBuilders.get("/employees/{id}", employeeTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Override
    @RoleTestRoot
    public void writeAuthorized() throws Exception {
        Employee employee = createAndSaveEmployee();
        this.mockMvc.perform(RestDocumentationRequestBuilders.put("/employees/{id}/permissions", employee.getId())
                .content(objectMapper.writeValueAsString(createUpdatePermissionsRequest(employee)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    public void readUnauthorized() {
    }

    @Override
    @RoleTestAdmin
    public void writeUnauthorized() throws Exception {
        Employee employee = createAndSaveEmployee();
        this.mockMvc.perform(RestDocumentationRequestBuilders.put("/employees/{id}/permissions", employee.getId())
                .content(objectMapper.writeValueAsString(createUpdatePermissionsRequest(employee)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @Transactional
    private void createEmployee() {
        this.employeeTest = createAndSaveEmployee();
        this.employeeTest.getPermissions().add(new Permission("ROLE_ROOT"));
        this.employeeRepository.save(this.employeeTest);
    }
}
