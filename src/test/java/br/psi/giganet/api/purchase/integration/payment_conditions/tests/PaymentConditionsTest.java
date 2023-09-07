package br.psi.giganet.api.purchase.integration.payment_conditions.tests;

import br.psi.giganet.api.purchase.config.security.repository.PermissionRepository;
import br.psi.giganet.api.purchase.employees.repository.EmployeeRepository;
import br.psi.giganet.api.purchase.integration.payment_conditions.annotations.RoleTestPaymentConditionsRead;
import br.psi.giganet.api.purchase.integration.payment_conditions.annotations.RoleTestPaymentConditionsWrite;
import br.psi.giganet.api.purchase.integration.utils.BuilderIntegrationTest;
import br.psi.giganet.api.purchase.integration.utils.RolesIntegrationTest;
import br.psi.giganet.api.purchase.integration.utils.annotations.RoleTestAdmin;
import br.psi.giganet.api.purchase.integration.utils.annotations.RoleTestRoot;
import br.psi.giganet.api.purchase.payment_conditions.controller.request.PaymentConditionRequest;
import br.psi.giganet.api.purchase.payment_conditions.model.PaymentCondition;
import br.psi.giganet.api.purchase.payment_conditions.repository.PaymentConditionRepository;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.RequestFieldsSnippet;
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

public class PaymentConditionsTest extends BuilderIntegrationTest implements RolesIntegrationTest {

    private PaymentCondition paymentConditionTest;

    @Autowired
    public PaymentConditionsTest(
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository,
            PaymentConditionRepository paymentConditionRepository) {

        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;
        this.paymentConditionRepository = paymentConditionRepository;
        createCurrentUser();

        paymentConditionTest = createAndSavePaymentCondition();
    }

    @RoleTestRoot
    public void findAll() throws Exception {
        this.mockMvc.perform(get("/payment-conditions")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(fieldWithPath("[]").description("Lista de centro de custos encontrados"))
                                .andWithPrefix("[].",
                                        fieldWithPath("id").description("Código da condição de pagamento"),
                                        fieldWithPath("name").description("Nome"),
                                        fieldWithPath("numberOfInstallments").description("Número de parcelas do pagamento"),
                                        fieldWithPath("daysInterval").description("Intervalo entre as parcelas, em dias"),
                                        fieldWithPath("description").description("Descrição da condição de pagamento"))));
    }

    @RoleTestRoot
    public void findById() throws Exception {
        this.mockMvc.perform(get("/payment-conditions/{id}", paymentConditionTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código do centro de custo buscado")
                        ),
                        getResponse()));
    }

    @RoleTestRoot
    @Transactional
    public void insert() throws Exception {
        this.mockMvc.perform(post("/payment-conditions")
                .content(new ObjectMapper().writeValueAsString(createCostCenterRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        getRequest(),
                        getResponse()));
    }

    @RoleTestRoot
    @Transactional
    public void update() throws Exception {
        this.mockMvc.perform(put("/payment-conditions/{id}", paymentConditionTest.getId())
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
                        getRequest(),
                        getResponse()));
    }

    @Override
    @RoleTestPaymentConditionsRead
    public void readAuthorized() throws Exception {
        this.mockMvc.perform(get("/payment-conditions"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/payment-conditions/{id}", paymentConditionTest.getId()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    @RoleTestAdmin
    public void readUnauthorized() throws Exception {
        this.mockMvc.perform(get("/payment-conditions"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/payment-conditions/{id}", paymentConditionTest.getId()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @Override
    @RoleTestPaymentConditionsWrite
    public void writeAuthorized() throws Exception {
        this.mockMvc.perform(post("/payment-conditions")
                .content(new ObjectMapper().writeValueAsString(createCostCenterRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated());

        this.mockMvc.perform(put("/payment-conditions/{id}", paymentConditionTest.getId())
                .content(new ObjectMapper().writeValueAsString(createCostCenterRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    @RoleTestAdmin
    public void writeUnauthorized() throws Exception {
        this.mockMvc.perform(post("/payment-conditions")
                .content(new ObjectMapper().writeValueAsString(createCostCenterRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(put("/payment-conditions/{id}", paymentConditionTest.getId())
                .content(new ObjectMapper().writeValueAsString(createCostCenterRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    private ResponseFieldsSnippet getResponse() {
        return responseFields(
                fieldWithPath("id").description("Código da condição de pagamento"),
                fieldWithPath("name").description("Nome"),
                fieldWithPath("numberOfInstallments").description("Número de parcelas do pagamento"),
                fieldWithPath("daysInterval").description("Intervalo entre as parcelas, em dias"),
                fieldWithPath("description").description("Descrição da condição de pagamento"));
    }

    private RequestFieldsSnippet getRequest() {
        return requestFields(
                fieldWithPath("name").description(
                        createDescriptionWithNotEmpty("Nome")),
                fieldWithPath("numberOfInstallments").description(
                        createDescriptionWithPositiveAndNotNull("Número de parcelas da condição de pagamento")),
                fieldWithPath("daysInterval").description(
                        createDescriptionWithPositiveAndNotNull("Intervalo entre as parcelas, em dias")),
                fieldWithPath("description")
                        .optional()
                        .type(JsonFieldType.STRING)
                        .description(createDescription("Descrição do centro de custo")));
    }

    private PaymentConditionRequest createCostCenterRequest() {
        PaymentConditionRequest request = new PaymentConditionRequest();
        request.setName("Condição de pagamento");
        request.setDescription("Descrição de teste");
        request.setDaysInterval(getRandomId());
        request.setNumberOfInstallments(getRandomId());

        return request;
    }

}
