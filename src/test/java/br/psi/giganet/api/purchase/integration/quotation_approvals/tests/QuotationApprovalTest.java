package br.psi.giganet.api.purchase.integration.quotation_approvals.tests;

import br.psi.giganet.api.purchase.approvals.repository.ApprovalRepository;
import br.psi.giganet.api.purchase.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.config.security.repository.PermissionRepository;
import br.psi.giganet.api.purchase.cost_center.repository.CostCenterRepository;
import br.psi.giganet.api.purchase.employees.repository.EmployeeRepository;
import br.psi.giganet.api.purchase.integration.utils.BuilderIntegrationTest;
import br.psi.giganet.api.purchase.integration.utils.RolesIntegrationTest;
import br.psi.giganet.api.purchase.integration.utils.annotations.RoleTestAdmin;
import br.psi.giganet.api.purchase.integration.utils.annotations.RoleTestRoot;
import br.psi.giganet.api.purchase.locations.repository.LocationRepository;
import br.psi.giganet.api.purchase.payment_conditions.repository.PaymentConditionRepository;
import br.psi.giganet.api.purchase.products.categories.repository.ProductCategoryRepository;
import br.psi.giganet.api.purchase.products.repository.ProductRepository;
import br.psi.giganet.api.purchase.projects.repository.ProjectRepository;
import br.psi.giganet.api.purchase.purchase_requests.repository.PurchaseRequestRepository;
import br.psi.giganet.api.purchase.quotation_approvals.controller.request.InsertQuotationApprovalRequest;
import br.psi.giganet.api.purchase.quotation_approvals.controller.request.UpdateQuotationApprovalRequest;
import br.psi.giganet.api.purchase.quotation_approvals.controller.security.RoleQuotationApprovalsRead;
import br.psi.giganet.api.purchase.quotation_approvals.controller.security.RoleQuotationApprovalsWrite;
import br.psi.giganet.api.purchase.quotation_approvals.model.QuotationApproval;
import br.psi.giganet.api.purchase.quotation_approvals.repository.QuotationApprovalRepository;
import br.psi.giganet.api.purchase.quotations.repository.QuotationRepository;
import br.psi.giganet.api.purchase.suppliers.repository.SupplierRepository;
import br.psi.giganet.api.purchase.suppliers.taxes.repository.TaxRepository;
import br.psi.giganet.api.purchase.units.repository.UnitRepository;
import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.ZonedDateTime;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class QuotationApprovalTest extends BuilderIntegrationTest implements RolesIntegrationTest {

    private QuotationApproval approvalTest;

    @Autowired
    public QuotationApprovalTest(
            ProductRepository productRepository,
            SupplierRepository supplierRepository,
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository,
            PurchaseRequestRepository purchaseRequestRepository,
            ApprovalRepository approvalRepository,
            QuotationRepository quotationRepository,
            QuotationApprovalRepository quotationApprovalRepository,
            UnitRepository unitRepository,
            ProductCategoryRepository productCategoryRepository,
            CostCenterRepository costCenterRepository,
            PaymentConditionRepository paymentConditionRepository,
            TaxRepository taxRepository,
            BranchOfficeRepository branchOfficeRepository,
            ProjectRepository projectRepository,
            LocationRepository locationRepository
    ) {
        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;
        this.purchaseRequestRepository = purchaseRequestRepository;
        this.approvalRepository = approvalRepository;
        this.quotationRepository = quotationRepository;
        this.quotationApprovalRepository = quotationApprovalRepository;
        this.costCenterRepository = costCenterRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.unitRepository = unitRepository;
        this.paymentConditionRepository = paymentConditionRepository;
        this.taxRepository = taxRepository;
        this.branchOfficeRepository = branchOfficeRepository;
        this.projectRepository = projectRepository;
        this.locationRepository = locationRepository;

        createCurrentUser();

        approvalTest = createAndSaveQuotationApproval();
        createNotificationPermissions();
    }

    @RoleTestRoot
    public void findAll() throws Exception {
        this.mockMvc.perform(get("/quotation-approvals")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                getListQuotationApprovalProjectionDescription()));
    }

    @RoleTestRoot
    public void findAllWithoutQuotation() throws Exception {
        this.mockMvc.perform(get("/quotation-approvals")
                .param("withoutQuotation", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestParameters(
                                        parameterWithName("withoutQuotation")
                                                .description(createDescription(
                                                        "Apenas uma flag para indicar qual será o formato do retorno",
                                                        "Será retornado apenas o ID da cotação, otimizando o tempo de resposta"))),
                                getListQuotationApprovalProjectionWithoutQuotationDescription()));
    }

    @RoleTestRoot
    public void findById() throws Exception {
        this.mockMvc.perform(get("/quotation-approvals/{id}", approvalTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("id").description(createDescriptionWithNotNull("Código da aprovação procurada"))),
                                getQuotationApprovalResponseDescription()));
    }

    @RoleTestRoot
    public void findByIdWithTrace() throws Exception {
        this.mockMvc.perform(get("/quotation-approvals/trace/{id}", approvalTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("id").description(createDescriptionWithNotNull("Código da aprovação procurada"))),
                                getQuotationApprovalWithTraceResponseDescription()));
    }

    @RoleTestRoot
    @Transactional
    public void insert() throws Exception {
        this.mockMvc.perform(post("/quotation-approvals")
                .content(objectMapper.writeValueAsString(createValidInsertApproval()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.evaluation", Matchers.is(ProcessStatus.APPROVED.toString())))
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        fieldWithPath("quotation").description(createDescriptionWithNotNull("Código da cotação a ser avaliada")),
                                        fieldWithPath("note")
                                                .optional()
                                                .type(JsonFieldType.STRING)
                                                .description(createDescription("Observações, caso existam")),
                                        fieldWithPath("evaluation").description(createDescriptionWithNotNull(
                                                "Avaliação final da cotação"))),
                                getQuotationApprovalResponseDescription()));
    }

    @RoleTestRoot
    @Transactional
    public void evaluateHandler() throws Exception {
        this.mockMvc.perform(put("/quotation-approvals/{id}", approvalTest.getId())
                .content(objectMapper.writeValueAsString(createValidEvaluateHandlerApproval()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evaluation", Matchers.is(ProcessStatus.APPROVED.toString())))
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("id").description("Código da aprovação a ser avaliada")),
                                requestFields(
                                        fieldWithPath("id").description(createDescriptionWithNotNull("Código da aprovação a ser avaliada")),
                                        fieldWithPath("note")
                                                .optional()
                                                .type(JsonFieldType.STRING)
                                                .description(createDescription("Observações, caso existam")),
                                        fieldWithPath("evaluation").description(createDescriptionWithNotNull(
                                                "Avaliação final da cotação"))),
                                getQuotationApprovalResponseDescription()));
    }

    @Override
    @RoleQuotationApprovalsRead
    public void readAuthorized() throws Exception {
        this.mockMvc.perform(get("/quotation-approvals"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/quotation-approvals/{id}", approvalTest.getId()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/quotation-approvals")
                .param("withoutQuotation", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    @RoleQuotationApprovalsWrite
    public void writeAuthorized() throws Exception {
        this.mockMvc.perform(post("/quotation-approvals")
                .content(objectMapper.writeValueAsString(createValidInsertApproval()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.evaluation", Matchers.is(ProcessStatus.APPROVED.toString())));

        this.mockMvc.perform(put("/quotation-approvals/{id}", approvalTest.getId())
                .content(objectMapper.writeValueAsString(createValidEvaluateHandlerApproval()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evaluation", Matchers.is(ProcessStatus.APPROVED.toString())));

    }

    @Override
    @RoleTestAdmin
    public void readUnauthorized() throws Exception {
        this.mockMvc.perform(get("/quotation-approvals"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/quotation-approvals/{id}", approvalTest.getId()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/quotation-approvals")
                .param("withoutQuotation", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @Override
    @RoleTestAdmin
    public void writeUnauthorized() throws Exception {
        this.mockMvc.perform(post("/quotation-approvals")
                .content(objectMapper.writeValueAsString(createValidInsertApproval()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(put("/quotation-approvals/{id}", approvalTest.getId())
                .content(objectMapper.writeValueAsString(createValidEvaluateHandlerApproval()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @RoleTestRoot
    @Transactional
    public void rejectApprovalHandler() throws Exception {
        this.mockMvc.perform(put("/quotation-approvals/{id}", approvalTest.getId())
                .content(objectMapper.writeValueAsString(createRejectedEvaluateHandlerApproval()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evaluation", Matchers.is(ProcessStatus.REJECTED.toString())));
    }

    @RoleTestRoot
    @Transactional
    public void approveAndRejectInSequence() throws Exception {
        this.mockMvc.perform(put("/quotation-approvals/{id}", approvalTest.getId())
                .content(objectMapper.writeValueAsString(createValidEvaluateHandlerApproval()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evaluation", Matchers.is(ProcessStatus.APPROVED.toString())));

        this.mockMvc.perform(put("/quotation-approvals/{id}", approvalTest.getId())
                .content(objectMapper.writeValueAsString(createRejectedEvaluateHandlerApproval()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", Matchers.not(Matchers.empty())));

        this.mockMvc.perform(get("/quotation-approvals/{id}", approvalTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evaluation", Matchers.is(ProcessStatus.APPROVED.toString())));
    }

    private InsertQuotationApprovalRequest createValidInsertApproval() {
        final var request = new InsertQuotationApprovalRequest();
        request.setEvaluation(ProcessStatus.APPROVED);
        request.setQuotation(approvalTest.getQuotation().getId());
        request.setNote("Observacao de teste " + getRandomId());

        return request;
    }

    private UpdateQuotationApprovalRequest createValidEvaluateHandlerApproval() {
        final var request = new UpdateQuotationApprovalRequest();
        request.setEvaluation(ProcessStatus.APPROVED);
        request.setId(approvalTest.getId());
        request.setNote("Observacao de teste " + getRandomId());

        return request;
    }

    private UpdateQuotationApprovalRequest createRejectedEvaluateHandlerApproval() {
        final var request = new UpdateQuotationApprovalRequest();
        request.setEvaluation(ProcessStatus.REJECTED);
        request.setId(approvalTest.getId());
        request.setNote("Observacao de teste " + getRandomId());

        return request;
    }

    private ResponseFieldsSnippet getListQuotationApprovalProjectionWithoutQuotationDescription() {
        return responseFields(fieldWithPath("[]")
                .description("Lista de todas as aprovações ordenadas de forma" +
                        " decrescente de acordo com a data de criação do registro"))
                .andWithPrefix("[].",
                        fieldWithPath("id").description("Código da aprovação"),
                        fieldWithPath("quotation").description("Cotação avaliada"),
                        fieldWithPath("total").description("Valor total da cotação"),
                        fieldWithPath("branchOffice").description("Filial associada"),
                        fieldWithPath("requester").description("Solicitante"),
                        fieldWithPath("responsible").optional().type(JsonFieldType.OBJECT).description("Responsável pela avaliação"),
                        fieldWithPath("date").description("Data de criação do registro. Exemplo: " + ZonedDateTime.now().toString()),
                        fieldWithPath("evaluation").description("Avaliação atual da cotação"))
                .andWithPrefix("[].requester.",
                        fieldWithPath("id").description("Código do solicitante"),
                        fieldWithPath("name").description("Nome do solicitante"))
                .andWithPrefix("[].responsible.",
                        fieldWithPath("id").description("Código do responsável"),
                        fieldWithPath("name").description("Nome do responsável"))
                .andWithPrefix("[].branchOffice.",
                        fieldWithPath("id").description("Código da filial"),
                        fieldWithPath("name").description("Nome da filial"),
                        fieldWithPath("shortName").description("Nome abreviado da filial"));
    }

    private ResponseFieldsSnippet getListQuotationApprovalProjectionDescription() {
        return responseFields(fieldWithPath("[]")
                .description("Lista de todas as aprovações ordenadas de forma" +
                        " decrescente de acordo com a data de criação do registro"))
                .andWithPrefix("[].",
                        fieldWithPath("id").description("Código da aprovação"),
                        fieldWithPath("quotation").description("Cotação avaliada"),
                        fieldWithPath("requester").description("Solicitante"),
                        fieldWithPath("responsible").optional().type(JsonFieldType.OBJECT).description("Responsável pela avaliação"),
                        fieldWithPath("date").description("Data de criação do registro. Exemplo: " + ZonedDateTime.now().toString()),
                        fieldWithPath("evaluation").description("Avaliação atual da cotação"))
                .andWithPrefix("[].requester.",
                        fieldWithPath("id").description("Código do solicitante"),
                        fieldWithPath("name").description("Nome do solicitante"))
                .andWithPrefix("[].responsible.",
                        fieldWithPath("id").description("Código do responsável"),
                        fieldWithPath("name").description("Nome do responsável"))
                .andWithPrefix("[].quotation.",
                        fieldWithPath("id").description("Código da solicitação de compra"),
                        fieldWithPath("note").description("Observações sobre a cotação"),
                        fieldWithPath("total").description("Valor total da cotação, incluindo o valor total de todos os itens e o valor do frete"),
                        fieldWithPath("description").optional().type(JsonFieldType.STRING).description("Descrição sobre a cotação"),
                        fieldWithPath("date").description("Data da cotação. Exemplo: " + ZonedDateTime.now().toString()),
                        fieldWithPath("responsible").description("Responsável pela cotação"),
                        fieldWithPath("branchOffice").description("Filial associada"),
                        fieldWithPath("status").description("Status atual da cotação"))
                .andWithPrefix("[].quotation.branchOffice.",
                        fieldWithPath("id").description("Código da filial"),
                        fieldWithPath("name").description("Nome da filial"),
                        fieldWithPath("shortName").description("Nome abreviado da filial"))
                .andWithPrefix("[].quotation.responsible.",
                        fieldWithPath("id").description("Código do responsável"),
                        fieldWithPath("name").description("Nome do responsável"));
    }

    private ResponseFieldsSnippet getQuotationApprovalResponseDescription() {
        return responseFields(
                fieldWithPath("id").description("Código da aprovação"),
                fieldWithPath("quotation").description("Cotação avaliada"),
                fieldWithPath("requester").description("Solicitante"),
                fieldWithPath("responsible").optional().type(JsonFieldType.OBJECT).description("Responsável pela avaliação"),
                fieldWithPath("note").optional().type(JsonFieldType.STRING).description("Observações da avaliação, caso exista"),
                fieldWithPath("date")
                        .optional()
                        .type(JsonFieldType.STRING)
                        .description("Data de criação do registro. Exemplo: " + ZonedDateTime.now().toString()),
                fieldWithPath("evaluation").description("Avaliação atual da cotação"))
                .andWithPrefix("requester.",
                        fieldWithPath("id").description("Código do solicitante"),
                        fieldWithPath("name").description("Nome do solicitante"))
                .andWithPrefix("responsible.",
                        fieldWithPath("id").description("Código do responsável"),
                        fieldWithPath("name").description("Nome do responsável"))
                .andWithPrefix("quotation.",
                        fieldWithPath("id").description("Código da cotação"),
                        fieldWithPath("responsible")
                                .type(JsonFieldType.OBJECT)
                                .description("Responsável pela cotação"),
                        fieldWithPath("date").description("Data da cotação. Exemplo: " + ZonedDateTime.now().toString()),
                        fieldWithPath("note").description("Observação"),
                        fieldWithPath("total").description("Valor total da cotação, incluindo o valor total de todos os itens e o valor do frete"),
                        fieldWithPath("status").description("Status atual da cotação"),
                        fieldWithPath("costCenter").description("Centro de custo da cotação"),
                        fieldWithPath("branchOffice").description("Filial associada a cotação"),
                        fieldWithPath("externalLink").optional().type(JsonFieldType.STRING)
                                .description("Link para algum site externo relacionado com a cotação"),
                        fieldWithPath("location").optional().type(JsonFieldType.OBJECT)
                                .description("Código ID da localidade associada a cotação, caso exista"),
                        fieldWithPath("project").optional().type(JsonFieldType.OBJECT)
                                .description("Código ID do projeto associado a cotação, caso exista"),
                        fieldWithPath("description").optional().type(JsonFieldType.STRING).description("Descrição sobre a cotação"),
                        fieldWithPath("freight").description("Frete da cotação"),
                        fieldWithPath("paymentCondition").description("Condição de pagamento para a cotação"),
                        fieldWithPath("items").description("Lista de itens cotados"),
                        fieldWithPath("dateOfNeed").optional()
                                .type(JsonFieldType.STRING)
                                .description("Data de necessidade. Exemplo: " + LocalDate.now().toString()))
                .andWithPrefix("quotation.freight.",
                        fieldWithPath("type").description("Tipo do frete. FOB ou CIF"),
                        fieldWithPath("price").description("Preço total do frete"))
                .andWithPrefix("quotation.project.",
                        fieldWithPath("id").description("Código do projeto"),
                        fieldWithPath("name").description("Nome do projeto"),
                        fieldWithPath("description").description("Descrição do projeto"))
                .andWithPrefix("quotation.location.",
                        fieldWithPath("id").description("Código da localidade"),
                        fieldWithPath("name").description("Nome da localidade"),
                        fieldWithPath("description").description("Descrição da localidade"))
                .andWithPrefix("quotation.paymentCondition.",
                        fieldWithPath("id").description("Código do relacionamento entre a cotação e a condição de pagamento"),
                        fieldWithPath("condition").description("Condição de pagamento selecionada"),
                        fieldWithPath("dueDates").description("Datas dos vencimentos das parcelas"))
                .andWithPrefix("quotation.paymentCondition.condition.",
                        fieldWithPath("id").description("Código da condição de pagamento"),
                        fieldWithPath("name").description("Nome"),
                        fieldWithPath("numberOfInstallments").description("Número de parcelas do pagamento"),
                        fieldWithPath("daysInterval").description("Intervalo entre as parcelas, em dias"),
                        fieldWithPath("description").description("Descrição da condição de pagamento"))
                .andWithPrefix("quotation.paymentCondition.dueDates[].",
                        fieldWithPath("id").description("Código do relacionemnto entre a condição de pagamento selecionada e a data de vencimento"),
                        fieldWithPath("dueDate").description("Data do vencimento"))
                .andWithPrefix("quotation.costCenter.",
                        fieldWithPath("id").description("Código do centro de custo"),
                        fieldWithPath("name").description("Nome do centro de custo"),
                        fieldWithPath("description").description("Descrição do centro de custo"))
                .andWithPrefix("quotation.branchOffice.",
                        fieldWithPath("id").description("Código da filial"),
                        fieldWithPath("name").description("Nome da filial"),
                        fieldWithPath("shortName").description("Nome abreviado da filial"))
                .andWithPrefix("quotation.responsible.",
                        fieldWithPath("id").description("Código do responsável"),
                        fieldWithPath("name").description("Nome do responsável"))
                .andWithPrefix("quotation.items[].",
                        fieldWithPath("id").description("Código do relacionamento entre produto e a cotação"),
                        fieldWithPath("product").description("Produto solicitado"),
                        fieldWithPath("quantity").description("Quantidade solicitada"),
                        fieldWithPath("ipi").description("Valor do IPI cotado para o produto, em porcentagem"),
                        fieldWithPath("icms").description("Valor do ICMS cotado para o produto, em porcentagem"),
                        fieldWithPath("price").description("Preço unitário selecionado para o item"),
                        fieldWithPath("discount").description("O valor do desconto em reais, caso exista"),
                        fieldWithPath("total").description("Preço total selecionado para o item"),
                        fieldWithPath("approval")
                                .optional()
                                .type(JsonFieldType.NUMBER)
                                .description("Codigo da aprovação, caso este esteja associado a alguma"),
                        fieldWithPath("approvalItem")
                                .optional()
                                .type(JsonFieldType.NUMBER)
                                .description("Codigo do item da aprovação, caso este esteja associado"),
                        fieldWithPath("suppliers").description("Lista dos fornecedores cotados para o respectivo item"),
                        fieldWithPath("unit").description("Unidade selecionada para o produto"))
                .andWithPrefix("quotation.items[].unit.",
                        fieldWithPath("id").description("Código da unidade selecionada para o produto"),
                        fieldWithPath("abbreviation").description("Abreviação do nome da unidade selecionada para o produto"),
                        fieldWithPath("name").description("Nome da unidade selecionada para o produto"))
                .andWithPrefix("quotation.items[].product.",
                        fieldWithPath("id").description("Código do produto oriundo do banco de dados"),
                        fieldWithPath("code").description("Código de identificação do produto como por exemplo código serial"),
                        fieldWithPath("name").description("Nome do produto"),
                        fieldWithPath("unit").description("Unidade padrão para o respectivo produto"),
                        fieldWithPath("manufacturer").description("Nome do fabricante do produto"))
                .andWithPrefix("quotation.items[].product.unit.",
                        fieldWithPath("id").description("Código da unidade padrão do produto"),
                        fieldWithPath("name").description("Nome da unidade padrão do produto"),
                        fieldWithPath("abbreviation").description("Abreviação utilizada para a unidade padrão do produto"))
                .andWithPrefix("quotation.items[].suppliers[].",
                        fieldWithPath("id").description("Código do relacionamento entre o item cotado e o fornecedor cotado"),
                        fieldWithPath("supplier").description("Fornecedor cotado"),
                        fieldWithPath("quantity").description("Quantidade selecionada"),
                        fieldWithPath("ipi").description("Valor do IPI cotado para o produto no respectivo fornecedor, em porcentagem"),
                        fieldWithPath("icms").description("Valor do ICMS cotado para o produto no respectivo fornecedor, em porcentagem"),
                        fieldWithPath("unit").description("Unidade cotada para o produto no respectivo fornecedor"),
                        fieldWithPath("price").description("Preço unitário para o item no respectivo fornecedor"),
                        fieldWithPath("discount").description("O valor do desconto em reais para o respectivo fornecedor, caso exista"),
                        fieldWithPath("total").description("Preço total para o item no respectivo fornecedor"),
                        fieldWithPath("isSelected").description("Indica se o respectivo fornecedor foi o selecionado para o item cotado"))
                .andWithPrefix("quotation.items[].suppliers[].unit.",
                        fieldWithPath("id").description("Código da unidade cotada para o produto no respectivo fornecedor"),
                        fieldWithPath("abbreviation").description("Abreviação do nome da unidade cotada para o produto no respectivo fornecedor"),
                        fieldWithPath("name").description("Nome da unidade cotada para o produto no respectivo fornecedor"))
                .andWithPrefix("quotation.items[].suppliers[].supplier.",
                        fieldWithPath("id").description("Código do fornecedor"),
                        fieldWithPath("name").description("Nome do fornecedor"));
    }

    private ResponseFieldsSnippet getQuotationApprovalWithTraceResponseDescription() {
        return responseFields(
                fieldWithPath("id").description("Código da aprovação"),
                fieldWithPath("quotation").description("Cotação avaliada"),
                fieldWithPath("requester").description("Solicitante"),
                fieldWithPath("responsible").optional().type(JsonFieldType.OBJECT).description("Responsável pela avaliação"),
                fieldWithPath("note").optional().type(JsonFieldType.STRING).description("Observações da avaliação, caso exista"),
                fieldWithPath("date")
                        .optional()
                        .type(JsonFieldType.STRING)
                        .description("Data de criação do registro. Exemplo: " + ZonedDateTime.now().toString()),
                fieldWithPath("evaluation").description("Avaliação atual da cotação"))
                .andWithPrefix("requester.",
                        fieldWithPath("id").description("Código do solicitante"),
                        fieldWithPath("name").description("Nome do solicitante"))
                .andWithPrefix("responsible.",
                        fieldWithPath("id").description("Código do responsável"),
                        fieldWithPath("name").description("Nome do responsável"))
                .andWithPrefix("quotation.",
                        fieldWithPath("id").description("Código da cotação"),
                        fieldWithPath("responsible")
                                .type(JsonFieldType.OBJECT)
                                .description("Responsável pela cotação"),
                        fieldWithPath("date").description("Data da cotação. Exemplo: " + ZonedDateTime.now().toString()),
                        fieldWithPath("note").description("Observação"),
                        fieldWithPath("total").description("Valor total da cotação, incluindo o valor total de todos os itens e o valor do frete"),
                        fieldWithPath("status").description("Status atual da cotação"),
                        fieldWithPath("costCenter").description("Centro de custo da cotação"),
                        fieldWithPath("branchOffice").description("Filial associada a cotação"),
                        fieldWithPath("externalLink").optional().type(JsonFieldType.STRING)
                                .description("Link para algum site externo relacionado com a cotação"),
                        fieldWithPath("location").optional().type(JsonFieldType.OBJECT)
                                .description("Código ID da localidade associada a cotação, caso exista"),
                        fieldWithPath("project").optional().type(JsonFieldType.OBJECT)
                                .description("Código ID do projeto associado a cotação, caso exista"),
                        fieldWithPath("description").optional().type(JsonFieldType.STRING).description("Descrição sobre a cotação"),
                        fieldWithPath("freight").description("Frete da cotação"),
                        fieldWithPath("paymentCondition").description("Condição de pagamento para a cotação"),
                        fieldWithPath("items").description("Lista de itens cotados"),
                        fieldWithPath("dateOfNeed").optional()
                                .type(JsonFieldType.STRING)
                                .description("Data de necessidade. Exemplo: " + LocalDate.now().toString()))
                .andWithPrefix("quotation.freight.",
                        fieldWithPath("type").description("Tipo do frete. FOB ou CIF"),
                        fieldWithPath("price").description("Preço total do frete"))
                .andWithPrefix("quotation.project.",
                        fieldWithPath("id").description("Código do projeto"),
                        fieldWithPath("name").description("Nome do projeto"),
                        fieldWithPath("description").description("Descrição do projeto"))
                .andWithPrefix("quotation.location.",
                        fieldWithPath("id").description("Código da localidade"),
                        fieldWithPath("name").description("Nome da localidade"),
                        fieldWithPath("description").description("Descrição da localidade"))
                .andWithPrefix("quotation.paymentCondition.",
                        fieldWithPath("id").description("Código do relacionamento entre a cotação e a condição de pagamento"),
                        fieldWithPath("condition").description("Condição de pagamento selecionada"),
                        fieldWithPath("dueDates").description("Datas dos vencimentos das parcelas"))
                .andWithPrefix("quotation.paymentCondition.condition.",
                        fieldWithPath("id").description("Código da condição de pagamento"),
                        fieldWithPath("name").description("Nome"),
                        fieldWithPath("numberOfInstallments").description("Número de parcelas do pagamento"),
                        fieldWithPath("daysInterval").description("Intervalo entre as parcelas, em dias"),
                        fieldWithPath("description").description("Descrição da condição de pagamento"))
                .andWithPrefix("quotation.paymentCondition.dueDates[].",
                        fieldWithPath("id").description("Código do relacionemnto entre a condição de pagamento selecionada e a data de vencimento"),
                        fieldWithPath("dueDate").description("Data do vencimento"))
                .andWithPrefix("quotation.costCenter.",
                        fieldWithPath("id").description("Código do centro de custo"),
                        fieldWithPath("name").description("Nome do centro de custo"),
                        fieldWithPath("description").description("Descrição do centro de custo"))
                .andWithPrefix("quotation.branchOffice.",
                        fieldWithPath("id").description("Código da filial"),
                        fieldWithPath("name").description("Nome da filial"),
                        fieldWithPath("shortName").description("Nome abreviado da filial"))
                .andWithPrefix("quotation.responsible.",
                        fieldWithPath("id").description("Código do responsável"),
                        fieldWithPath("name").description("Nome do responsável"))
                .andWithPrefix("quotation.items[].",
                        fieldWithPath("id").description("Código do relacionamento entre produto e a cotação"),
                        fieldWithPath("product").description("Produto solicitado"),
                        fieldWithPath("quantity").description("Quantidade solicitada"),
                        fieldWithPath("ipi").description("Valor do IPI cotado para o produto, em porcentagem"),
                        fieldWithPath("icms").description("Valor do ICMS cotado para o produto, em porcentagem"),
                        fieldWithPath("price").description("Preço unitário selecionado para o item"),
                        fieldWithPath("discount").description("O valor do desconto em reais, caso exista"),
                        fieldWithPath("total").description("Preço total selecionado para o item"),
                        fieldWithPath("approvalItem")
                                .optional()
                                .type(JsonFieldType.OBJECT)
                                .description("Aprovação do item, caso este esteja associada"),
                        fieldWithPath("suppliers").description("Lista dos fornecedores cotados para o respectivo item"),
                        fieldWithPath("unit").description("Unidade selecionada para o produto"))
                .andWithPrefix("quotation.items[].approvalItem.",
                        fieldWithPath("id")
                                .description("Código do item de aprovação"),
                        fieldWithPath("product")
                                .description("Produto solicitado"),
                        fieldWithPath("unit")
                                .description("Unidade solicitada"),
                        fieldWithPath("quantity")
                                .description("Quantidade solicitada"),
                        fieldWithPath("status")
                                .description("Status de aprovação do produto"),
                        fieldWithPath("approval")
                                .description("Código da aprovação associada"),
                        fieldWithPath("approvedTrace").optional().type(JsonFieldType.OBJECT)
                                .description("Objeto contendo as quantidades já aprovadas para o respectivo item, considerando unidade padrão do item"),
                        fieldWithPath("pendingTrace").optional().type(JsonFieldType.OBJECT)
                                .description("Objeto contendo as quantidades pendentes para aprovação para o respectivo item, considerando unidade padrão do item"))
                .andWithPrefix("quotation.items[].approvalItem.approvedTrace.",
                        fieldWithPath("value").optional().type(JsonFieldType.NUMBER)
                                .description(createDescription(
                                        "Quantidade de itens aprovados referentes a esta aprovação",
                                        "A quantidade será sempre informado na unidade padrão do item")),
                        fieldWithPath("unit").optional().type(JsonFieldType.STRING)
                                .description(createDescription("Abreviação da unidade padrão do item")))
                .andWithPrefix("quotation.items[].approvalItem.pendingTrace.",
                        fieldWithPath("value").optional().type(JsonFieldType.NUMBER)
                                .description(createDescription(
                                        "Quantidade de itens pendentes referentes a esta aprovação",
                                        "A quantidade será sempre informado na unidade padrão do item")),
                        fieldWithPath("unit").optional().type(JsonFieldType.STRING)
                                .description(createDescription("Abreviação da unidade padrão do item")))
                .andWithPrefix("quotation.items[].approvalItem.product.",
                        fieldWithPath("id")
                                .description("Código do produto oriundo do banco de dados"),
                        fieldWithPath("code")
                                .description("Código de identificação do produto como por exemplo código serial"),
                        fieldWithPath("name")
                                .description("Nome do produto"),
                        fieldWithPath("unit")
                                .description("Unidade padrão atribuida ao produto"),
                        fieldWithPath("manufacturer")
                                .description("Nome do fabricante do produto"))
                .andWithPrefix("quotation.items[].approvalItem.product.unit.",
                        fieldWithPath("id")
                                .description("Código da unidade padrão do produto"),
                        fieldWithPath("abbreviation")
                                .description("Abreviação do nome da unidade padrão do produto"),
                        fieldWithPath("name")
                                .description("Nome da unidade padrão do produto"))
                .andWithPrefix("quotation.items[].approvalItem.unit.",
                        fieldWithPath("id")
                                .description("Código da unidade solicitada"),
                        fieldWithPath("abbreviation")
                                .description("Abreviação do nome da unidade solicitada"),
                        fieldWithPath("name")
                                .description("Nome da unidade solicitada"))
                .andWithPrefix("quotation.items[].unit.",
                        fieldWithPath("id").description("Código da unidade selecionada para o produto"),
                        fieldWithPath("abbreviation").description("Abreviação do nome da unidade selecionada para o produto"),
                        fieldWithPath("name").description("Nome da unidade selecionada para o produto"))
                .andWithPrefix("quotation.items[].product.",
                        fieldWithPath("id").description("Código do produto oriundo do banco de dados"),
                        fieldWithPath("code").description("Código de identificação do produto como por exemplo código serial"),
                        fieldWithPath("name").description("Nome do produto"),
                        fieldWithPath("unit").description("Unidade padrão para o respectivo produto"),
                        fieldWithPath("manufacturer").description("Nome do fabricante do produto"))
                .andWithPrefix("quotation.items[].product.unit.",
                        fieldWithPath("id").description("Código da unidade padrão do produto"),
                        fieldWithPath("name").description("Nome da unidade padrão do produto"),
                        fieldWithPath("abbreviation").description("Abreviação utilizada para a unidade padrão do produto"))
                .andWithPrefix("quotation.items[].suppliers[].",
                        fieldWithPath("id").description("Código do relacionamento entre o item cotado e o fornecedor cotado"),
                        fieldWithPath("supplier").description("Fornecedor cotado"),
                        fieldWithPath("quantity").description("Quantidade selecionada"),
                        fieldWithPath("ipi").description("Valor do IPI cotado para o produto no respectivo fornecedor, em porcentagem"),
                        fieldWithPath("icms").description("Valor do ICMS cotado para o produto no respectivo fornecedor, em porcentagem"),
                        fieldWithPath("unit").description("Unidade cotada para o produto no respectivo fornecedor"),
                        fieldWithPath("price").description("Preço unitário para o item no respectivo fornecedor"),
                        fieldWithPath("discount").description("O valor do desconto em reais no respectivo fornecedor, caso exista"),
                        fieldWithPath("total").description("Preço total para o item no respectivo fornecedor"),
                        fieldWithPath("isSelected").description("Indica se o respectivo fornecedor foi o selecionado para o item cotado"))
                .andWithPrefix("quotation.items[].suppliers[].unit.",
                        fieldWithPath("id").description("Código da unidade cotada para o produto no respectivo fornecedor"),
                        fieldWithPath("abbreviation").description("Abreviação do nome da unidade cotada para o produto no respectivo fornecedor"),
                        fieldWithPath("name").description("Nome da unidade cotada para o produto no respectivo fornecedor"))
                .andWithPrefix("quotation.items[].suppliers[].supplier.",
                        fieldWithPath("id").description("Código do fornecedor"),
                        fieldWithPath("name").description("Nome do fornecedor"));
    }

}
