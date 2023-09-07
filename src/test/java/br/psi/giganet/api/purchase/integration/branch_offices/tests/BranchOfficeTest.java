package br.psi.giganet.api.purchase.integration.branch_offices.tests;

import br.psi.giganet.api.purchase.branch_offices.controller.request.BranchOfficeAddressRequest;
import br.psi.giganet.api.purchase.branch_offices.controller.request.InsertBranchOfficeRequest;
import br.psi.giganet.api.purchase.branch_offices.controller.request.UpdateBranchOfficeRequest;
import br.psi.giganet.api.purchase.branch_offices.model.BranchOffice;
import br.psi.giganet.api.purchase.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.api.purchase.config.security.repository.PermissionRepository;
import br.psi.giganet.api.purchase.employees.repository.EmployeeRepository;
import br.psi.giganet.api.purchase.integration.branch_offices.annotations.RoleTestBranchOfficesRead;
import br.psi.giganet.api.purchase.integration.branch_offices.annotations.RoleTestBranchOfficesWrite;
import br.psi.giganet.api.purchase.integration.utils.BuilderIntegrationTest;
import br.psi.giganet.api.purchase.integration.utils.RolesIntegrationTest;
import br.psi.giganet.api.purchase.integration.utils.annotations.RoleTestAdmin;
import br.psi.giganet.api.purchase.integration.utils.annotations.RoleTestRoot;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
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

public class BranchOfficeTest extends BuilderIntegrationTest implements RolesIntegrationTest {

    @Autowired
    public BranchOfficeTest(
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository,
            BranchOfficeRepository branchOfficeRepository) {

        this.branchOfficeRepository = branchOfficeRepository;
        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;

        createCurrentUser();
    }

    @RoleTestRoot
    public void findAll() throws Exception {
        createAndSaveBranchOffice();

        this.mockMvc.perform(get("/branch-offices")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(fieldWithPath("[]").description("Lista de filiais"))
                                .andWithPrefix("[].", getBranchOfficeProjection())
                ));
    }

    @RoleTestRoot
    public void findById() throws Exception {
        BranchOffice office = createAndSaveBranchOffice();

        this.mockMvc.perform(get("/branch-offices/{id}", office.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código da filial buscada")
                        ),
                        responseFields(getBranchOfficeResponse())
                                .andWithPrefix("address.", getBranchOfficeAddressResponse())
                ));
    }

    @RoleTestRoot
    @Transactional
    public void insert() throws Exception {
        this.mockMvc.perform(post("/branch-offices")
                .content(new ObjectMapper().writeValueAsString(createInsertBranchOfficeRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("name").description(
                                        createDescriptionWithNotEmpty("Nome da filial")),
                                fieldWithPath("shortName").description(
                                        createDescriptionWithNotEmpty("Nome abreviado da filial")),
                                fieldWithPath("cnpj").description(
                                        createDescriptionWithNotEmpty("CNPJ da filial", "Deve ser um valor CNPJ válido")),
                                fieldWithPath("stateRegistration")
                                        .optional()
                                        .type(JsonFieldType.STRING)
                                        .description(createDescription("Inscrição Estadual")),
                                fieldWithPath("telephone")
                                        .type(JsonFieldType.STRING)
                                        .optional()
                                        .description("Telefone de contato"),
                                fieldWithPath("address")
                                        .type(JsonFieldType.OBJECT)
                                        .description(createDescriptionWithNotNull("Endereço")))
                                .andWithPrefix("address.",
                                        fieldWithPath("street").description(
                                                createDescriptionWithNotEmpty("Rua")),
                                        fieldWithPath("postalCode").description(
                                                createDescriptionWithNotEmpty("CEP", "Deve ser informado apenas os 8 números")),
                                        fieldWithPath("complement")
                                                .optional()
                                                .type(JsonFieldType.STRING)
                                                .description("Complemento"),
                                        fieldWithPath("number").description(
                                                createDescriptionWithNotEmpty("Número")),
                                        fieldWithPath("district").description(
                                                createDescriptionWithNotEmpty("Bairro")),
                                        fieldWithPath("city").description(
                                                createDescriptionWithNotEmpty("Cidade")),
                                        fieldWithPath("state").description(
                                                createDescriptionWithNotEmpty("Estado"))),
                        responseFields(getBranchOfficeResponse())
                                .andWithPrefix("address.", getBranchOfficeAddressResponse())));
    }

    @RoleTestRoot
    @Transactional
    public void update() throws Exception {
        BranchOffice office = createAndSaveBranchOffice();

        this.mockMvc.perform(put("/branch-offices/{id}", office.getId())
                .content(new ObjectMapper().writeValueAsString(createUpdateBranchOfficeRequest(office.getId())))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código da filial a ser atualizada")
                        ),
                        requestFields(
                                fieldWithPath("id").description(createDescriptionWithNotNull("Código da filial a ser atualizada")),
                                fieldWithPath("name").description(
                                        createDescriptionWithNotEmpty("Nome da filial")),
                                fieldWithPath("shortName").description(
                                        createDescriptionWithNotEmpty("Nome abreviado da filial")),
                                fieldWithPath("cnpj").description(
                                        createDescriptionWithNotEmpty("CNPJ da filial", "Deve ser um valor CNPJ válido")),
                                fieldWithPath("stateRegistration")
                                        .optional()
                                        .type(JsonFieldType.STRING)
                                        .description(createDescription("Inscrição Estadual")),
                                fieldWithPath("telephone")
                                        .type(JsonFieldType.STRING)
                                        .optional()
                                        .description("Telefone de contato"),
                                fieldWithPath("address")
                                        .type(JsonFieldType.OBJECT)
                                        .description(createDescriptionWithNotNull("Endereço")))
                                .andWithPrefix("address.",
                                        fieldWithPath("street").description(
                                                createDescriptionWithNotEmpty("Rua")),
                                        fieldWithPath("postalCode").description(
                                                createDescriptionWithNotEmpty("CEP", "Deve ser informado apenas os 8 números")),
                                        fieldWithPath("complement")
                                                .optional()
                                                .type(JsonFieldType.STRING)
                                                .description("Complemento"),
                                        fieldWithPath("number").description(
                                                createDescriptionWithNotEmpty("Número")),
                                        fieldWithPath("district").description(
                                                createDescriptionWithNotEmpty("Bairro")),
                                        fieldWithPath("city").description(
                                                createDescriptionWithNotEmpty("Cidade")),
                                        fieldWithPath("state").description(
                                                createDescriptionWithNotEmpty("Estado"))),
                        responseFields(getBranchOfficeResponse())
                                .andWithPrefix("address.", getBranchOfficeAddressResponse())));
    }

    @Override
    @Transactional
    @RoleTestBranchOfficesRead
    public void readAuthorized() throws Exception {
        BranchOffice office = createAndSaveBranchOffice();

        this.mockMvc.perform(get("/branch-offices"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/branch-offices/{id}", office.getId()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    @Transactional
    @RoleTestAdmin
    public void readUnauthorized() throws Exception {
        BranchOffice office = createAndSaveBranchOffice();

        this.mockMvc.perform(get("/branch-offices"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/branch-offices/{id}", office.getId()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @Override
    @Transactional
    @RoleTestBranchOfficesWrite
    public void writeAuthorized() throws Exception {
        this.mockMvc.perform(post("/branch-offices")
                .content(new ObjectMapper().writeValueAsString(createInsertBranchOfficeRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated());

        BranchOffice office = createAndSaveBranchOffice();
        this.mockMvc.perform(put("/branch-offices/{id}", office.getId())
                .content(new ObjectMapper().writeValueAsString(createUpdateBranchOfficeRequest(office.getId())))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    @Transactional
    @RoleTestAdmin
    public void writeUnauthorized() throws Exception {
        this.mockMvc.perform(post("/branch-offices")
                .content(new ObjectMapper().writeValueAsString(createInsertBranchOfficeRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        BranchOffice office = createAndSaveBranchOffice();
        this.mockMvc.perform(put("/branch-offices/{id}", office.getId())
                .content(new ObjectMapper().writeValueAsString(createUpdateBranchOfficeRequest(office.getId())))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    private FieldDescriptor[] getBranchOfficeProjection() {
        return new FieldDescriptor[]{
                fieldWithPath("id").description("Código da filial cadastrada"),
                fieldWithPath("name").description("Nome da filial"),
                fieldWithPath("shortName").description("Nome abreviado da filial"),
        };
    }

    private FieldDescriptor[] getBranchOfficeResponse() {
        return new FieldDescriptor[]{
                fieldWithPath("id").description("Código da filial cadastrada"),
                fieldWithPath("name").description("Nome da filial"),
                fieldWithPath("shortName").description("Nome abreviado da filial"),
                fieldWithPath("cnpj").description("CNPJ da filial"),
                fieldWithPath("telephone").description("Telefone da filial"),
                fieldWithPath("stateRegistration").description("Registro estadual"),
                fieldWithPath("address").description("Endereço")
        };
    }

    private FieldDescriptor[] getBranchOfficeAddressResponse() {
        return new FieldDescriptor[]{
                fieldWithPath("street").description(
                        createDescriptionWithNotEmpty("Rua")),
                fieldWithPath("postalCode").description(
                        createDescriptionWithNotEmpty("CEP")),
                fieldWithPath("complement")
                        .optional()
                        .type(JsonFieldType.STRING)
                        .description("Complemento"),
                fieldWithPath("number").description(
                        createDescriptionWithNotEmpty("Número")),
                fieldWithPath("district").description(
                        createDescriptionWithNotEmpty("Bairro")),
                fieldWithPath("city").description(
                        createDescriptionWithNotEmpty("Cidade")),
                fieldWithPath("state").description(
                        createDescriptionWithNotEmpty("Estado"))
        };
    }

    private InsertBranchOfficeRequest createInsertBranchOfficeRequest() {
        InsertBranchOfficeRequest request = new InsertBranchOfficeRequest();
        request.setName("Filial Ipatinga");
        request.setShortName("Ipa");
        request.setAddress(new BranchOfficeAddressRequest());
        request.getAddress().setStreet("Rua teste " + getRandomId());
        request.getAddress().setNumber("120");
        request.getAddress().setDistrict("Horto");
        request.getAddress().setPostalCode("35160294");
        request.getAddress().setCity("Ipatinga");
        request.getAddress().setState("MG");
        request.setTelephone("3138338989");
        request.setStateRegistration("12341235899");
        request.setCnpj("12764848000176");
        return request;
    }

    private UpdateBranchOfficeRequest createUpdateBranchOfficeRequest(Long id) {
        UpdateBranchOfficeRequest request = new UpdateBranchOfficeRequest();
        request.setId(id);
        request.setName("Filial X");
        request.setShortName("Ipa");
        request.setAddress(new BranchOfficeAddressRequest());
        request.getAddress().setStreet("Rua teste " + getRandomId());
        request.getAddress().setNumber("120");
        request.getAddress().setDistrict("Horto");
        request.getAddress().setPostalCode("35160294");
        request.getAddress().setCity("Ipatinga");
        request.getAddress().setState("MG");
        request.setTelephone("3138338989");
        request.setStateRegistration("12341235899");
        request.setCnpj("12764848000176");
        return request;
    }

}
