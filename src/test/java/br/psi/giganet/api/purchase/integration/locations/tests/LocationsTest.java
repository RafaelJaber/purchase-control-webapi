package br.psi.giganet.api.purchase.integration.locations.tests;

import br.psi.giganet.api.purchase.branch_offices.model.BranchOffice;
import br.psi.giganet.api.purchase.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.api.purchase.config.security.repository.PermissionRepository;
import br.psi.giganet.api.purchase.employees.repository.EmployeeRepository;
import br.psi.giganet.api.purchase.integration.locations.annotations.RoleTestLocationsRead;
import br.psi.giganet.api.purchase.integration.locations.annotations.RoleTestLocationsWrite;
import br.psi.giganet.api.purchase.integration.utils.BuilderIntegrationTest;
import br.psi.giganet.api.purchase.integration.utils.RolesIntegrationTest;
import br.psi.giganet.api.purchase.integration.utils.annotations.RoleTestAdmin;
import br.psi.giganet.api.purchase.integration.utils.annotations.RoleTestRoot;
import br.psi.giganet.api.purchase.locations.controller.request.InsertLocationRequest;
import br.psi.giganet.api.purchase.locations.controller.request.UpdateLocationRequest;
import br.psi.giganet.api.purchase.locations.model.Location;
import br.psi.giganet.api.purchase.locations.repository.LocationRepository;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import javax.transaction.Transactional;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LocationsTest extends BuilderIntegrationTest implements RolesIntegrationTest {

    @Autowired
    public LocationsTest(
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository,
            LocationRepository locationRepository,
            BranchOfficeRepository branchOfficeRepository) {

        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;
        this.locationRepository = locationRepository;
        this.branchOfficeRepository = branchOfficeRepository;

    }

    @RoleTestRoot
    public void findAll() throws Exception {
        BranchOffice office = createAndSaveBranchOffice();
        for (int i = 0; i < 2; i++) {
            createAndSaveLocation(office);
        }
        this.mockMvc.perform(get("/locations")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        getResponseAsArray()));
    }

    @RoleTestRoot
    public void findByBranchOffice() throws Exception {
        BranchOffice office = createAndSaveBranchOffice();
        for (int i = 0; i < 2; i++) {
            createAndSaveLocation(office);
        }
        this.mockMvc.perform(RestDocumentationRequestBuilders.get("/locations/branch-offices/{office}", office.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("office").description("Código da filial associada a ser utilizada como filtro")),
                        getResponseAsArray()));
    }

    @RoleTestRoot
    public void findById() throws Exception {
        var location = createAndSaveLocation();
        this.mockMvc.perform(RestDocumentationRequestBuilders.get("/locations/{id}", location.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código da localidade buscada")
                        ),
                        getResponse()));
    }

    @RoleTestRoot
    @Transactional
    public void insert() throws Exception {
        this.mockMvc.perform(post("/locations")
                .content(new ObjectMapper().writeValueAsString(createInsertLocationRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("name").description(
                                        createDescriptionWithNotEmpty("Nome da localidade")),
                                fieldWithPath("branchOffice").description(
                                        createDescriptionWithNotNull("Filial associada")),
                                fieldWithPath("description")
                                        .optional()
                                        .type(JsonFieldType.STRING)
                                        .description("Descrição da localidade")),
                        getResponse()));
    }

    @RoleTestRoot
    @Transactional
    public void update() throws Exception {
        var location = createAndSaveLocation();
        this.mockMvc.perform(RestDocumentationRequestBuilders.put("/locations/{id}", location.getId())
                .content(new ObjectMapper().writeValueAsString(createUpdateLocationRequest(location)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código da localidade buscada")
                        ),
                        requestFields(
                                fieldWithPath("id").description("Código da localidade"),
                                fieldWithPath("name").description(
                                        createDescriptionWithNotEmpty("Nome da localidade")),
                                fieldWithPath("branchOffice").description(
                                        createDescriptionWithNotNull("Filial associada")),
                                fieldWithPath("description")
                                        .optional()
                                        .type(JsonFieldType.STRING)
                                        .description("Descrição da localidade")),
                        getResponse()));
    }

    @Override
    @RoleTestLocationsRead
    public void readAuthorized() throws Exception {
        var location = createAndSaveLocation();
        this.mockMvc.perform(get("/locations"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(RestDocumentationRequestBuilders.get("/locations/{id}", location.getId()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(RestDocumentationRequestBuilders.get("/locations/branch-offices/{office}", location.getBranchOffice().getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    @RoleTestAdmin
    public void readUnauthorized() throws Exception {
        var location = createAndSaveLocation();
        this.mockMvc.perform(get("/locations"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(RestDocumentationRequestBuilders.get("/locations/{id}", location.getId()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(RestDocumentationRequestBuilders.get("/locations/branch-offices/{office}", location.getBranchOffice().getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @Override
    @RoleTestLocationsWrite
    public void writeAuthorized() throws Exception {
        var location = createAndSaveLocation();
        this.mockMvc.perform(post("/locations")
                .content(new ObjectMapper().writeValueAsString(createInsertLocationRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated());

        this.mockMvc.perform(RestDocumentationRequestBuilders.put("/locations/{id}", location.getId())
                .content(new ObjectMapper().writeValueAsString(createUpdateLocationRequest(location)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    @RoleTestAdmin
    public void writeUnauthorized() throws Exception {
        var location = createAndSaveLocation();
        this.mockMvc.perform(post("/locations")
                .content(new ObjectMapper().writeValueAsString(createInsertLocationRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(RestDocumentationRequestBuilders.put("/locations/{id}", location.getId())
                .content(new ObjectMapper().writeValueAsString(createInsertLocationRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    private InsertLocationRequest createInsertLocationRequest() {
        return createInsertLocationRequest(createAndSaveBranchOffice());
    }

    private InsertLocationRequest createInsertLocationRequest(BranchOffice office) {
        InsertLocationRequest request = new InsertLocationRequest();
        request.setName("Localidade X");
        request.setDescription("Descrição da localidade " + getRandomId());
        request.setBranchOffice(office.getId());
        return request;
    }

    private UpdateLocationRequest createUpdateLocationRequest(Location location) {
        UpdateLocationRequest request = new UpdateLocationRequest();
        request.setName("Localidade X");
        request.setDescription("Descrição da localidade " + getRandomId());
        request.setBranchOffice(location.getBranchOffice().getId());
        request.setId(location.getId());
        return request;
    }

    private ResponseFieldsSnippet getResponse() {
        return responseFields(
                fieldWithPath("id").description("Código da localidade"),
                fieldWithPath("name").description("Nome da localidade"),
                fieldWithPath("branchOffice").description("Filial associada"),
                fieldWithPath("description").description("Descrição da localidade"))
                .andWithPrefix("branchOffice.",
                        fieldWithPath("id").description("Código da filial"),
                        fieldWithPath("name").description("Nome da filial"),
                        fieldWithPath("shortName").description("Nome abreviado da filial"));
    }

    private ResponseFieldsSnippet getResponseAsArray() {
        return responseFields(fieldWithPath("[]").description("Lista com todos os registros encontrados"))
                .andWithPrefix("[].",
                        fieldWithPath("id").description("Código da localidade"),
                        fieldWithPath("name").description("Nome da localidade"),
                        fieldWithPath("description").description("Descrição da localidade"));
    }
}
