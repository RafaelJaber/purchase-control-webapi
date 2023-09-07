package br.psi.giganet.api.purchase.integration.projects.tests;

import br.psi.giganet.api.purchase.config.security.repository.PermissionRepository;
import br.psi.giganet.api.purchase.employees.repository.EmployeeRepository;
import br.psi.giganet.api.purchase.integration.projects.annotations.RoleTestProjectsRead;
import br.psi.giganet.api.purchase.integration.projects.annotations.RoleTestProjectsWrite;
import br.psi.giganet.api.purchase.integration.utils.BuilderIntegrationTest;
import br.psi.giganet.api.purchase.integration.utils.RolesIntegrationTest;
import br.psi.giganet.api.purchase.integration.utils.annotations.RoleTestAdmin;
import br.psi.giganet.api.purchase.integration.utils.annotations.RoleTestRoot;
import br.psi.giganet.api.purchase.projects.controller.request.ProjectRequest;
import br.psi.giganet.api.purchase.projects.repository.ProjectRepository;
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

public class ProjectsTest extends BuilderIntegrationTest implements RolesIntegrationTest {

    @Autowired
    public ProjectsTest(
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository,
            ProjectRepository projectRepository) {

        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;
        this.projectRepository = projectRepository;

    }

    @RoleTestRoot
    public void findAll() throws Exception {
        for (int i = 0; i < 2; i++) {
            createAndSaveProject();
        }
        this.mockMvc.perform(get("/projects")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        getResponseAsArray()));
    }

    @RoleTestRoot
    public void findById() throws Exception {
        var project = createAndSaveProject();
        this.mockMvc.perform(get("/projects/{id}", project.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código do projeto buscado")
                        ),
                        getResponse()));
    }

    @RoleTestRoot
    @Transactional
    public void insert() throws Exception {
        this.mockMvc.perform(post("/projects")
                .content(new ObjectMapper().writeValueAsString(createProjectRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("name").description(
                                        createDescriptionWithNotEmpty("Nome do projeto")),
                                fieldWithPath("description")
                                        .optional()
                                        .type(JsonFieldType.STRING)
                                        .description("Descrição do projeto")),
                        getResponse()));
    }

    @RoleTestRoot
    @Transactional
    public void update() throws Exception {
        var project = createAndSaveProject();
        this.mockMvc.perform(put("/projects/{id}", project.getId())
                .content(new ObjectMapper().writeValueAsString(createProjectRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código do projeto buscado")
                        ),
                        requestFields(
                                fieldWithPath("name").description(
                                        createDescriptionWithNotEmpty("Nome do projeto")),
                                fieldWithPath("description")
                                        .optional()
                                        .type(JsonFieldType.STRING)
                                        .description("Descrição do projeto")),
                        getResponse()));
    }

    @Override
    @RoleTestProjectsRead
    public void readAuthorized() throws Exception {
        var project = createAndSaveProject();
        this.mockMvc.perform(get("/projects"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/projects/{id}", project.getId()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    @RoleTestAdmin
    public void readUnauthorized() throws Exception {
        var project = createAndSaveProject();
        this.mockMvc.perform(get("/projects"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/projects/{id}", project.getId()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @Override
    @RoleTestProjectsWrite
    public void writeAuthorized() throws Exception {
        var project = createAndSaveProject();
        this.mockMvc.perform(post("/projects")
                .content(new ObjectMapper().writeValueAsString(createProjectRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated());

        this.mockMvc.perform(put("/projects/{id}", project.getId())
                .content(new ObjectMapper().writeValueAsString(createProjectRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    @RoleTestAdmin
    public void writeUnauthorized() throws Exception {
        var project = createAndSaveProject();
        this.mockMvc.perform(post("/projects")
                .content(new ObjectMapper().writeValueAsString(createProjectRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(put("/projects/{id}", project.getId())
                .content(new ObjectMapper().writeValueAsString(createProjectRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    private ProjectRequest createProjectRequest() {
        ProjectRequest request = new ProjectRequest();
        request.setName("Projeto X");
        request.setDescription("Descrição do projeto " + getRandomId());
        return request;
    }

    private ResponseFieldsSnippet getResponse() {
        return responseFields(
                fieldWithPath("id").description("Código do projeto"),
                fieldWithPath("name").description("Nome do projeto"),
                fieldWithPath("description").description("Descrição do projeto"));
    }

    private ResponseFieldsSnippet getResponseAsArray() {
        return responseFields(fieldWithPath("[]").description("Lista com todos os registros encontrados"))
                .andWithPrefix("[].",
                        fieldWithPath("id").description("Código do projeto"),
                        fieldWithPath("name").description("Nome do projeto"),
                        fieldWithPath("description").description("Descrição do projeto"));
    }
}
