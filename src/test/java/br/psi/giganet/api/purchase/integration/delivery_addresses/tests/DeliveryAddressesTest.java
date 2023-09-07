package br.psi.giganet.api.purchase.integration.delivery_addresses.tests;

import br.psi.giganet.api.purchase.common.address.model.Address;
import br.psi.giganet.api.purchase.config.security.repository.PermissionRepository;
import br.psi.giganet.api.purchase.delivery_addresses.controller.request.DeliveryAddressRequest;
import br.psi.giganet.api.purchase.delivery_addresses.model.DeliveryAddress;
import br.psi.giganet.api.purchase.delivery_addresses.repository.DeliveryAddressRepository;
import br.psi.giganet.api.purchase.employees.repository.EmployeeRepository;
import br.psi.giganet.api.purchase.integration.delivery_addresses.annotations.RoleTestDeliveryAddressesRead;
import br.psi.giganet.api.purchase.integration.delivery_addresses.annotations.RoleTestDeliveryAddressesWrite;
import br.psi.giganet.api.purchase.integration.utils.BuilderIntegrationTest;
import br.psi.giganet.api.purchase.integration.utils.RolesIntegrationTest;
import br.psi.giganet.api.purchase.integration.utils.annotations.RoleTestAdmin;
import br.psi.giganet.api.purchase.integration.utils.annotations.RoleTestRoot;
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

public class DeliveryAddressesTest extends BuilderIntegrationTest implements RolesIntegrationTest {

    private DeliveryAddress addressTest;

    @Autowired
    public DeliveryAddressesTest(
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository,
            DeliveryAddressRepository deliveryAddressRepository) {

        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;
        this.deliveryAddressRepository = deliveryAddressRepository;

        addressTest = createAndSaveDeliveryAddress();
    }

    @RoleTestRoot
    public void findAll() throws Exception {
        this.mockMvc.perform(get("/delivery-addresses")
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
        this.mockMvc.perform(get("/delivery-addresses/{id}", addressTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código do endereço de entrega buscado")
                        ),
                        getResponse()));
    }

    @RoleTestRoot
    @Transactional
    public void insert() throws Exception {
        this.mockMvc.perform(post("/delivery-addresses")
                .content(new ObjectMapper().writeValueAsString(createDeliveryAddressRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("name").description(
                                        createDescriptionWithNotEmpty("Nome")),
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
                        getResponse()));
    }

    @RoleTestRoot
    @Transactional
    public void update() throws Exception {
        this.mockMvc.perform(put("/delivery-addresses/{id}", addressTest.getId())
                .content(new ObjectMapper().writeValueAsString(createDeliveryAddressRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código do endereço de entrega a ser atualizado")
                        ),
                        requestFields(
                                fieldWithPath("name").description(
                                        createDescriptionWithNotEmpty("Nome")),
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
                        getResponse()));
    }

    @Override
    @RoleTestDeliveryAddressesRead
    public void readAuthorized() throws Exception {
        this.mockMvc.perform(get("/delivery-addresses"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/delivery-addresses/{id}", addressTest.getId()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    @RoleTestAdmin
    public void readUnauthorized() throws Exception {
        this.mockMvc.perform(get("/delivery-addresses"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/delivery-addresses/{id}", addressTest.getId()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @Override
    @RoleTestDeliveryAddressesWrite
    public void writeAuthorized() throws Exception {
        this.mockMvc.perform(post("/delivery-addresses")
                .content(new ObjectMapper().writeValueAsString(createDeliveryAddressRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated());

        this.mockMvc.perform(put("/delivery-addresses/{id}", addressTest.getId())
                .content(new ObjectMapper().writeValueAsString(createDeliveryAddressRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    @RoleTestAdmin
    public void writeUnauthorized() throws Exception {
        this.mockMvc.perform(post("/delivery-addresses")
                .content(new ObjectMapper().writeValueAsString(createDeliveryAddressRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(put("/delivery-addresses/{id}", addressTest.getId())
                .content(new ObjectMapper().writeValueAsString(createDeliveryAddressRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    private DeliveryAddressRequest createDeliveryAddressRequest() {
        DeliveryAddressRequest request = new DeliveryAddressRequest();
        request.setName("Endereço de entrega X");
        request.setAddress(new Address());
        request.getAddress().setStreet("Rua teste");
        request.getAddress().setNumber("120");
        request.getAddress().setDistrict("Horto");
        request.getAddress().setCity("Ipatinga");
        request.getAddress().setState("MG");
        request.getAddress().setPostalCode("35162201");
        request.getAddress().setComplement("Complement");
        return request;
    }

    private ResponseFieldsSnippet getResponse() {
        return responseFields(
                fieldWithPath("id").description("Código do endereço de entrega"),
                fieldWithPath("name").description("Nome"),
                fieldWithPath("address").description("Endereço de entrega"))
                .andWithPrefix("address.",
                        fieldWithPath("complement").description("Complemento"),
                        fieldWithPath("postalCode").description("CEP"),
                        fieldWithPath("street").description("Rua"),
                        fieldWithPath("number").description("Número"),
                        fieldWithPath("district").description("Bairro"),
                        fieldWithPath("city").description("Cidade"),
                        fieldWithPath("state").description("Estado"));
    }

    private ResponseFieldsSnippet getResponseAsArray() {
        return responseFields(fieldWithPath("[]").description("Lista com todos os registros encontrados"))
                .andWithPrefix("[].",
                        fieldWithPath("id").description("Código do endereço de entrega"),
                        fieldWithPath("name").description("Nome"),
                        fieldWithPath("address").description("Endereço de entrega"))
                .andWithPrefix("[].address.",
                        fieldWithPath("complement").description("Complemento"),
                        fieldWithPath("postalCode").description("CEP"),
                        fieldWithPath("street").description("Rua"),
                        fieldWithPath("number").description("Número"),
                        fieldWithPath("district").description("Bairro"),
                        fieldWithPath("city").description("Cidade"),
                        fieldWithPath("state").description("Estado"));
    }
}
