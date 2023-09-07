package br.psi.giganet.api.purchase.integration.approvals.tests;

import br.psi.giganet.api.purchase.approvals.controller.request.ApprovalEvaluateRequest;
import br.psi.giganet.api.purchase.approvals.controller.request.ApprovalItemEvaluate;
import br.psi.giganet.api.purchase.approvals.controller.security.RoleApprovalsRead;
import br.psi.giganet.api.purchase.approvals.controller.security.RoleApprovalsWrite;
import br.psi.giganet.api.purchase.approvals.model.Approval;
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
import br.psi.giganet.api.purchase.quotations.model.Quotation;
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
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ApprovalTest extends BuilderIntegrationTest implements RolesIntegrationTest {

    private Approval approvalTest;

    @Autowired
    public ApprovalTest(
            ProductRepository productRepository,
            SupplierRepository supplierRepository,
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository,
            PurchaseRequestRepository purchaseRequestRepository,
            ApprovalRepository approvalRepository,
            CostCenterRepository costCenterRepository,
            ProductCategoryRepository productCategoryRepository,
            UnitRepository unitRepository,
            TaxRepository taxRepository,
            BranchOfficeRepository branchOfficeRepository,
            QuotationRepository quotationRepository,
            PaymentConditionRepository paymentConditionRepository,
            ProjectRepository projectRepository,
            LocationRepository locationRepository
    ) {
        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;
        this.purchaseRequestRepository = purchaseRequestRepository;
        this.approvalRepository = approvalRepository;
        this.costCenterRepository = costCenterRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.unitRepository = unitRepository;
        this.taxRepository = taxRepository;
        this.branchOfficeRepository = branchOfficeRepository;
        this.quotationRepository = quotationRepository;
        this.paymentConditionRepository = paymentConditionRepository;
        this.projectRepository = projectRepository;
        this.locationRepository = locationRepository;

        createCurrentUser();

        approvalTest = createAndSaveApproval();
        createNotificationPermissions();
    }

    @RoleTestRoot
    public void findAll() throws Exception {
        this.mockMvc.perform(get("/approvals"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                responseFields(fieldWithPath("[]")
                                        .description("Lista de todas as aprovações ordenadas de forma" +
                                                " decrescente de acordo com a data de criação do registro"))
                                        .andWithPrefix("[].",
                                                fieldWithPath("id").description("Código da aprovação"),
                                                fieldWithPath("request").description("Código da solicitação a qual gerou a respectiva aprovação"),
                                                fieldWithPath("requester").description("Nome do solicitante"),
                                                fieldWithPath("description")
                                                        .optional()
                                                        .type(JsonFieldType.STRING)
                                                        .description(createDescription("Descrição da solicitação, caso exista")),
                                                fieldWithPath("approvalDate")
                                                        .optional()
                                                        .type(JsonFieldType.STRING)
                                                        .description("Data de aprovação/rejeição"),
                                                fieldWithPath("status").description("Status atual da solicitação a ser avaliada"))));
    }

    @RoleTestRoot
    public void findByStatus() throws Exception {
        Approval approval1 = createAndSaveApproval();
        approval1.setStatus(ProcessStatus.APPROVED);
        approvalRepository.save(approval1);

        Approval approval2 = createAndSaveApproval();
        approval2.setStatus(ProcessStatus.PARTIALLY_APPROVED);
        approvalRepository.save(approval2);

        this.mockMvc.perform(get("/approvals/statuses")
                .param("statuses", String.join(",",
                        ProcessStatus.APPROVED.toString(),
                        ProcessStatus.PARTIALLY_APPROVED.toString())))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestParameters(
                                        parameterWithName("statuses").description(
                                                createDescriptionWithNotEmpty("Lista com os status a serem filtrados"))),
                                responseFields(fieldWithPath("[]")
                                        .description("Lista de todas as aprovações ordenadas de forma" +
                                                " decrescente de acordo com a data de criação do registro"))
                                        .andWithPrefix("[].",
                                                fieldWithPath("id").description("Código da aprovação"),
                                                fieldWithPath("request").description("Código da solicitação a qual gerou a respectiva aprovação"),
                                                fieldWithPath("requester").description("Nome do solicitante"),
                                                fieldWithPath("description")
                                                        .optional()
                                                        .type(JsonFieldType.STRING)
                                                        .description(createDescription("Descrição da solicitação, caso exista")),
                                                fieldWithPath("approvalDate")
                                                        .optional()
                                                        .type(JsonFieldType.STRING)
                                                        .description("Data de aprovação/rejeição"),
                                                fieldWithPath("status").description("Status atual da solicitação a ser avaliada"))));
    }

    @RoleTestRoot
    public void findApprovalsAvailableToQuotation() throws Exception {
        Approval approval1 = createAndSaveApproval();
        approval1.setStatus(ProcessStatus.APPROVED);
        approval1.getItems().forEach(i -> i.setEvaluation(ProcessStatus.APPROVED));
        approvalRepository.save(approval1);

        this.mockMvc.perform(get("/approvals/quotation-available"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                responseFields(fieldWithPath("[]")
                                        .description("Lista de todas as aprovações disponíveis para cotação, descartando as finalizadas, ordenadas de forma" +
                                                " decrescente de acordo com a data de criação do registro"))
                                        .andWithPrefix("[].",
                                                fieldWithPath("id").description("Código da aprovação"),
                                                fieldWithPath("request").description("Código da solicitação a qual gerou a respectiva aprovação"),
                                                fieldWithPath("requester").description("Nome do solicitante"),
                                                fieldWithPath("description")
                                                        .optional()
                                                        .type(JsonFieldType.STRING)
                                                        .description(createDescription("Descrição da solicitação, caso exista")),
                                                fieldWithPath("approvalDate")
                                                        .optional()
                                                        .type(JsonFieldType.STRING)
                                                        .description("Data de aprovação/rejeição"),
                                                fieldWithPath("status").description("Status atual da solicitação a ser avaliada"))));
    }

    @RoleTestRoot
    public void findApprovedItemsByApproval() throws Exception {
        Approval approval = createAndSaveApproval();
        approval.setStatus(ProcessStatus.APPROVED);
        approval.getItems().forEach(i -> i.setEvaluation(ProcessStatus.APPROVED));
        approvalRepository.save(approval);

        this.mockMvc.perform(get("/approvals/{id}/items", approval.getId()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("id")
                                                .description(createDescriptionWithNotNull("Código da aprovação"))),
                                responseFields(
                                        fieldWithPath("[]")
                                                .description("Lista com os itens disponíveis para uma cotação em uma aprovação"))
                                        .andWithPrefix("[].",
                                                fieldWithPath("id")
                                                        .description("Código do item de aprovação"),
                                                fieldWithPath("item")
                                                        .description("Objeto contendo as informações do item de aprovação"),
                                                fieldWithPath("unit")
                                                        .description("Unidade solicitada"),
                                                fieldWithPath("quantity")
                                                        .description("Quantidade restante referente a solicitação realizada"),
                                                fieldWithPath("status")
                                                        .optional()
                                                        .type(JsonFieldType.STRING)
                                                        .description("Status de aprovação do produto"),
                                                fieldWithPath("approval")
                                                        .optional()
                                                        .type(JsonFieldType.NUMBER)
                                                        .description("Código da aprovação associada"))
                                        .andWithPrefix("[].unit.",
                                                fieldWithPath("id")
                                                        .description("Código da unidade solicitada"),
                                                fieldWithPath("abbreviation")
                                                        .description("Abreviação do nome da unidade solicitada"),
                                                fieldWithPath("name")
                                                        .description("Nome da unidade solicitada"))
                                        .andWithPrefix("[].item.",
                                                fieldWithPath("product")
                                                        .description("Produto solicitado"),
                                                fieldWithPath("availableUnits")
                                                        .description("Lista com as unidades possíveis para o respectivo item"))
                                        .andWithPrefix("[].item.product.",
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
                                        .andWithPrefix("[].item.product.unit.",
                                                fieldWithPath("id")
                                                        .description("Código da unidade padrão do produto"),
                                                fieldWithPath("abbreviation")
                                                        .description("Abreviação do nome da unidade padrão do produto"),
                                                fieldWithPath("name")
                                                        .description("Nome da unidade padrão do produto"))
                                        .andWithPrefix("[].item.availableUnits[].",
                                                fieldWithPath("id")
                                                        .description("Código da unidade solicitada"),
                                                fieldWithPath("abbreviation")
                                                        .description("Abreviação do nome da unidade solicitada"),
                                                fieldWithPath("name")
                                                        .description("Nome da unidade solicitada"))));
    }

    @RoleTestRoot
    public void findAllApprovalItemsAvailableToQuotation() throws Exception {
        Approval approval = createAndSaveApproval();
        approval.setStatus(ProcessStatus.APPROVED);
        approval.getItems().forEach(i -> i.setEvaluation(ProcessStatus.APPROVED));
        approvalRepository.save(approval);

        Quotation quotation = createAndSaveQuotation();

        this.mockMvc.perform(get("/approvals/quotation-available/items")
                .param("ignoredQuotation", quotation.getId().toString()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestParameters(parameterWithName("ignoredQuotation")
                                        .optional()
                                        .description(createDescription("Código da cotação a ser ignorada na consulta", "Opcional"))),
                                responseFields(
                                        fieldWithPath("[]").description("Lista com os itens disponíveis para cotação"))
                                        .andWithPrefix("[].",
                                                fieldWithPath("id")
                                                        .description("Código do item de aprovação"),
                                                fieldWithPath("item")
                                                        .description("Objeto contendo as informações do item de aprovação"),
                                                fieldWithPath("unit")
                                                        .description("Unidade solicitada"),
                                                fieldWithPath("quantity")
                                                        .description("Quantidade restante referente a solicitação realizada"),
                                                fieldWithPath("dateOfNeed")
                                                        .description("Data de necessidade"),
                                                fieldWithPath("status")
                                                        .optional()
                                                        .type(JsonFieldType.STRING)
                                                        .description("Status de aprovação do produto"),
                                                fieldWithPath("costCenter")
                                                        .description("Centro de custo associado"),
                                                fieldWithPath("branchOffice")
                                                        .description("Filial associada a solicitação de compra"),
                                                fieldWithPath("approval")
                                                        .optional()
                                                        .type(JsonFieldType.OBJECT)
                                                        .description("Aprovação associada"))
                                        .andWithPrefix("[].unit.",
                                                fieldWithPath("id")
                                                        .description("Código da unidade solicitada"),
                                                fieldWithPath("abbreviation")
                                                        .description("Abreviação do nome da unidade solicitada"),
                                                fieldWithPath("name")
                                                        .description("Nome da unidade solicitada"))
                                        .andWithPrefix("[].costCenter.",
                                                fieldWithPath("id")
                                                        .description("Código do centro de custo"),
                                                fieldWithPath("description")
                                                        .description("Descrição do centro de custo"),
                                                fieldWithPath("name")
                                                        .description("Nome do centro de custo"))
                                        .andWithPrefix("[].branchOffice.",
                                                fieldWithPath("id")
                                                        .description("Código da filial"),
                                                fieldWithPath("shortName")
                                                        .description("Nome abreviado da filial"),
                                                fieldWithPath("name")
                                                        .description("Nome da filial"))
                                        .andWithPrefix("[].item.",
                                                fieldWithPath("product")
                                                        .description("Produto solicitado"),
                                                fieldWithPath("availableUnits")
                                                        .description("Lista com as unidades possíveis para o respectivo item"))
                                        .andWithPrefix("[].item.product.",
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
                                        .andWithPrefix("[].item.product.unit.",
                                                fieldWithPath("id")
                                                        .description("Código da unidade padrão do produto"),
                                                fieldWithPath("abbreviation")
                                                        .description("Abreviação do nome da unidade padrão do produto"),
                                                fieldWithPath("name")
                                                        .description("Nome da unidade padrão do produto"))
                                        .andWithPrefix("[].item.availableUnits[].",
                                                fieldWithPath("id")
                                                        .description("Código da unidade solicitada"),
                                                fieldWithPath("abbreviation")
                                                        .description("Abreviação do nome da unidade solicitada"),
                                                fieldWithPath("name")
                                                        .description("Nome da unidade solicitada"))
                                        .andWithPrefix("[].approval.",
                                                fieldWithPath("id").description("Código da aprovação"),
                                                fieldWithPath("request").description("Código da solicitação a qual gerou a respectiva aprovação"),
                                                fieldWithPath("requester").description("Nome do solicitante"),
                                                fieldWithPath("description")
                                                        .optional()
                                                        .type(JsonFieldType.STRING)
                                                        .description(createDescription("Descrição da solicitação, caso exista")),
                                                fieldWithPath("approvalDate")
                                                        .optional()
                                                        .type(JsonFieldType.STRING)
                                                        .description("Data de aprovação/rejeição"),
                                                fieldWithPath("status").description("Status atual da solicitação a ser avaliada"))));
    }

    @RoleTestRoot
    public void findById() throws Exception {
        this.mockMvc.perform(get("/approvals/{id}", approvalTest.getId()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("id")
                                                .description(createDescriptionWithNotNull("Código da aprovação procurada"))),
                                getResponse()));
    }

    @RoleTestRoot
    public void findWithApprovalAndAvailableUnitsById() throws Exception {
        this.mockMvc.perform(get("/approvals/{id}", approvalTest.getId())
                .param("withUnits", ""))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("id")
                                                .description(createDescriptionWithNotNull("Código da aprovação procurada"))),
                                requestParameters(
                                        parameterWithName("withUnits")
                                                .description(createDescriptionWithNotEmpty(
                                                        "Parametro que determina que este endpoint seja associado",
                                                        "O valor associado é irrelevante, mas é necessário o parametro estar na URL"))),
                                getResponseWithAvailableUnits()));
    }

    @RoleTestRoot
    public void findByItemId() throws Exception {
        this.mockMvc.perform(get("/approvals/items/{id}", approvalTest.getItems().get(0).getId()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("id")
                                                .description(createDescriptionWithNotNull("Código do item de aprovação procurado"))),
                                responseFields(
                                        fieldWithPath("id")
                                                .description("Código do item de aprovação"),
                                        fieldWithPath("item")
                                                .description("Objeto contendo as informações do item de aprovação"),
                                        fieldWithPath("unit")
                                                .description("Unidade solicitada"),
                                        fieldWithPath("quantity")
                                                .description("Quantidade solicitada"),
                                        fieldWithPath("status")
                                                .description("Status de aprovação do produto"),
                                        fieldWithPath("approval")
                                                .description("Código da aprovação associada"))
                                        .andWithPrefix("unit.",
                                                fieldWithPath("id")
                                                        .description("Código da unidade solicitada"),
                                                fieldWithPath("abbreviation")
                                                        .description("Abreviação do nome da unidade solicitada"),
                                                fieldWithPath("name")
                                                        .description("Nome da unidade solicitada"))
                                        .andWithPrefix("item.",
                                                fieldWithPath("product")
                                                        .description("Produto solicitado"),
                                                fieldWithPath("availableUnits")
                                                        .description("Lista com as unidades possíveis para o respectivo item"))
                                        .andWithPrefix("item.product.",
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
                                        .andWithPrefix("item.product.unit.",
                                                fieldWithPath("id")
                                                        .description("Código da unidade padrão do produto"),
                                                fieldWithPath("abbreviation")
                                                        .description("Abreviação do nome da unidade padrão do produto"),
                                                fieldWithPath("name")
                                                        .description("Nome da unidade padrão do produto"))
                                        .andWithPrefix("item.availableUnits[].",
                                                fieldWithPath("id")
                                                        .description("Código da unidade solicitada"),
                                                fieldWithPath("abbreviation")
                                                        .description("Abreviação do nome da unidade solicitada"),
                                                fieldWithPath("name")
                                                        .description("Nome da unidade solicitada"))));
    }

    @RoleTestRoot
    @Transactional
    public void evaluateHandler() throws Exception {
        this.mockMvc.perform(put("/approvals/{id}/evaluate", approvalTest.getId())
                .content(objectMapper.writeValueAsString(createValidEvaluateHandlerApproval()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.is(ProcessStatus.APPROVED.toString())))
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("id").description("Código da aprovação a ser avaliada")),
                                requestFields(
                                        fieldWithPath("note")
                                                .optional()
                                                .type(JsonFieldType.STRING)
                                                .description(createDescription("Observações, caso existam")),
                                        fieldWithPath("description")
                                                .optional()
                                                .type(JsonFieldType.STRING)
                                                .description(createDescription("Descrição da solicitação, caso exista")),
                                        fieldWithPath("items")
                                                .description(createDescriptionWithNotNull("Lista de itens a ser avaliados")))
                                        .andWithPrefix("items[].",
                                                fieldWithPath("id")
                                                        .optional()
                                                        .type(JsonFieldType.NUMBER)
                                                        .description(createDescriptionWithNotNull(
                                                                "Código do respectivo relacionamento entre o produto e a " +
                                                                        "aprovação")),
                                                fieldWithPath("status")
                                                        .description(createDescriptionWithNotEmpty("A avaliação do respectivo item"))),
                                getResponse()));
    }

    @RoleTestRoot
    public void markItemAsDiscarded() throws Exception {
        Approval approval = createAndSaveApproval();
        approval.getItems().get(0).setEvaluation(ProcessStatus.APPROVED);
        approvalRepository.save(approval);

        this.mockMvc.perform(post("/approvals/items/{id}/discard", approval.getItems().get(0).getId()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("id")
                                                .description(createDescriptionWithNotNull("Código do item de aprovação a ser descartado"))),
                                responseFields(
                                        fieldWithPath("id")
                                                .description("Código do relacionamento entre produto e a aprovação"),
                                        fieldWithPath("product")
                                                .description("Produto solicitado"),
                                        fieldWithPath("unit")
                                                .description("Unidade solicitada"),
                                        fieldWithPath("quantity")
                                                .description("Quantidade solicitada"),
                                        fieldWithPath("status")
                                                .description("Status de aprovação do produto"))
                                        .andWithPrefix("product.",
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
                                        .andWithPrefix("product.unit.",
                                                fieldWithPath("id")
                                                        .description("Código da unidade padrão do produto"),
                                                fieldWithPath("abbreviation")
                                                        .description("Abreviação do nome da unidade padrão do produto"),
                                                fieldWithPath("name")
                                                        .description("Nome da unidade padrão do produto"))
                                        .andWithPrefix("unit.",
                                                fieldWithPath("id")
                                                        .description("Código da unidade solicitada"),
                                                fieldWithPath("abbreviation")
                                                        .description("Abreviação do nome da unidade solicitada"),
                                                fieldWithPath("name")
                                                        .description("Nome da unidade solicitada"))));
    }

    @Override
    @RoleApprovalsRead
    public void readAuthorized() throws Exception {
        // find all
        this.mockMvc.perform(get("/approvals"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        // find by status
        Approval approval1 = createAndSaveApproval();
        approval1.setStatus(ProcessStatus.APPROVED);
        approvalRepository.save(approval1);

        Approval approval2 = createAndSaveApproval();
        approval2.setStatus(ProcessStatus.PARTIALLY_APPROVED);
        approvalRepository.save(approval2);

        this.mockMvc.perform(get("/approvals/statuses")
                .param("statuses", String.join(",",
                        ProcessStatus.APPROVED.toString(),
                        ProcessStatus.PARTIALLY_APPROVED.toString())))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(2)));

        // find by id
        this.mockMvc.perform(get("/approvals/{id}", approvalTest.getId()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        // find by item id
        this.mockMvc.perform(get("/approvals/items/{id}", approvalTest.getItems().get(0).getId()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        // find by id with approvals and available units
        this.mockMvc.perform(get("/approvals/{id}", approvalTest.getId())
                .param("withUnits", ""))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/approvals/quotation-available/items"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    @RoleApprovalsWrite
    public void writeAuthorized() throws Exception {
        this.mockMvc.perform(put("/approvals/{id}/evaluate", approvalTest.getId())
                .content(objectMapper.writeValueAsString(createValidEvaluateHandlerApproval()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.is(ProcessStatus.APPROVED.toString())));

        Approval approval = createAndSaveApproval();
        approval.getItems().get(0).setEvaluation(ProcessStatus.APPROVED);
        approvalRepository.save(approval);
        this.mockMvc.perform(post("/approvals/items/{id}/discard", approval.getItems().get(0).getId()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    @RoleTestAdmin
    public void readUnauthorized() throws Exception {
        this.mockMvc.perform(get("/approvals"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/approvals/statuses")
                .param("statuses", String.join(",",
                        ProcessStatus.APPROVED.toString(),
                        ProcessStatus.PARTIALLY_APPROVED.toString())))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/approvals/{id}", approvalTest.getId()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/approvals/items/{id}", approvalTest.getItems().get(0).getId()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/approvals/{id}", approvalTest.getId())
                .param("withUnits", ""))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/approvals/quotation-available/items"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @Override
    @RoleTestAdmin
    public void writeUnauthorized() throws Exception {
        this.mockMvc.perform(put("/approvals/{id}/evaluate", approvalTest.getId())
                .content(objectMapper.writeValueAsString(createValidEvaluateHandlerApproval()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());


        Approval approval = createAndSaveApproval();
        approval.getItems().get(0).setEvaluation(ProcessStatus.APPROVED);
        approvalRepository.save(approval);
        this.mockMvc.perform(post("/approvals/items/{id}/discard", approval.getItems().get(0).getId()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @RoleTestRoot
    @Transactional
    public void rejectApprovalHandler() throws Exception {
        this.mockMvc.perform(put("/approvals/{id}/evaluate", approvalTest.getId())
                .content(objectMapper.writeValueAsString(createRejectedEvaluateHandlerApproval()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.is(ProcessStatus.REJECTED.toString())));
    }


    @RoleTestRoot
    @Transactional
    public void approveAndRejectInSequence() throws Exception {
        this.mockMvc.perform(put("/approvals/{id}/evaluate", approvalTest.getId())
                .content(objectMapper.writeValueAsString(createValidEvaluateHandlerApproval()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.is(ProcessStatus.APPROVED.toString())));

        this.mockMvc.perform(put("/approvals/{id}/evaluate", approvalTest.getId())
                .content(objectMapper.writeValueAsString(createRejectedEvaluateHandlerApproval()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", Matchers.not(Matchers.empty())));

        this.mockMvc.perform(get("/approvals/{id}", approvalTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.is(ProcessStatus.APPROVED.toString())));
    }

    @RoleTestRoot
    @Transactional
    public void invalidApproval() throws Exception {
        this.mockMvc.perform(put("/approvals/{id}/evaluate", approvalTest.getId())
                .content(objectMapper.writeValueAsString(createInvalidItemsApproval()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", Matchers.not(Matchers.empty())));

        this.mockMvc.perform(put("/approvals/{id}/evaluate", approvalTest.getId())
                .content(objectMapper.writeValueAsString(createEmptyItemsApproval()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", Matchers.not(Matchers.empty())));
    }

    private ApprovalEvaluateRequest createValidEvaluateHandlerApproval() {
        ApprovalEvaluateRequest request = new ApprovalEvaluateRequest();
        request.setNote("Reposição de estoque " + getRandomId());
        request.setItems(
                approvalTest
                        .getItems()
                        .stream()
                        .map(item -> new ApprovalItemEvaluate(item.getId(), ProcessStatus.APPROVED))
                        .collect(Collectors.toList())
        );
        return request;
    }

    private ApprovalEvaluateRequest createRejectedEvaluateHandlerApproval() {
        ApprovalEvaluateRequest request = new ApprovalEvaluateRequest();
        request.setNote("Reposição de estoque " + getRandomId());
        request.setItems(
                approvalTest
                        .getItems()
                        .stream()
                        .map(item -> new ApprovalItemEvaluate(item.getId(), ProcessStatus.REJECTED))
                        .collect(Collectors.toList())
        );
        return request;
    }

    private ApprovalEvaluateRequest createInvalidItemsApproval() {
        ApprovalEvaluateRequest request = new ApprovalEvaluateRequest();
        request.setNote("Reposição de estoque " + getRandomId());
        request.setItems(
                approvalTest
                        .getItems()
                        .stream()
                        .map(item -> new ApprovalItemEvaluate(item.getId(), null))
                        .collect(Collectors.toList())
        );
        return request;
    }

    private ApprovalEvaluateRequest createEmptyItemsApproval() {
        ApprovalEvaluateRequest request = new ApprovalEvaluateRequest();
        request.setNote("Reposição de estoque " + getRandomId());
        return request;
    }

    private ResponseFieldsSnippet getResponse() {
        return responseFields(
                fieldWithPath("id").description("Código da aprovação"),
                fieldWithPath("request")
                        .type(JsonFieldType.OBJECT)
                        .description("Solicitação de compra de forma reduzida"),
                fieldWithPath("approver")
                        .type(JsonFieldType.OBJECT)
                        .description("Funcionário o qual realizou a avaliação"),
                fieldWithPath("date")
                        .type(JsonFieldType.STRING)
                        .optional()
                        .description("Data da avaliação da aprovação. Exemplo: " + LocalDateTime.now().toString()),
                fieldWithPath("note")
                        .optional()
                        .type(JsonFieldType.STRING)
                        .description("Observação"),
                fieldWithPath("description")
                        .optional()
                        .type(JsonFieldType.STRING)
                        .description(createDescription("Descrição da solicitação, caso exista")),
                fieldWithPath("status").description("Status atual da aprovação"),
                fieldWithPath("items").description("Lista de itens solicitados"))
                .andWithPrefix("approver.",
                        fieldWithPath("id").description("Código do funcionário o qual realizou a avaliação"),
                        fieldWithPath("name").description("Nome do funcionário o qual realizou a avaliação"))
                .andWithPrefix("request.",
                        fieldWithPath("id").description("Código da solicitação"),
                        fieldWithPath("requester").description("Nome do solicitante"),
                        fieldWithPath("responsible").description("Nome do responsável indicado pelo solicitante"),
                        fieldWithPath("costCenter").description("Centro de custo"),
                        fieldWithPath("branchOffice").description("Filial associada"),
                        fieldWithPath("note").description("Observações da solicitação"),
                        fieldWithPath("reason").description("Motivo da solicitação"),
                        fieldWithPath("dateOfNeed").optional()
                                .type(JsonFieldType.STRING)
                                .description("Data de necessidade. Exemplo: " + LocalDate.now().toString()))
                .andWithPrefix("request.costCenter.",
                        fieldWithPath("id").description("Código do centro de custo"),
                        fieldWithPath("name").description("Nome"),
                        fieldWithPath("description").description("Descrição do centro de custo"))
                .andWithPrefix("request.branchOffice.",
                        fieldWithPath("id").description("Código da filial"),
                        fieldWithPath("name").description("Nome da filial"),
                        fieldWithPath("shortName").description("Nome abreviado da filial"))
                .andWithPrefix("items[].",
                        fieldWithPath("id")
                                .description("Código do relacionamento entre produto e a aprovação"),
                        fieldWithPath("product")
                                .description("Produto solicitado"),
                        fieldWithPath("unit")
                                .description("Unidade solicitada"),
                        fieldWithPath("quantity")
                                .description("Quantidade solicitada"),
                        fieldWithPath("status")
                                .description("Status de aprovação do produto"))
                .andWithPrefix("items[].product.",
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
                .andWithPrefix("items[].product.unit.",
                        fieldWithPath("id")
                                .description("Código da unidade padrão do produto"),
                        fieldWithPath("abbreviation")
                                .description("Abreviação do nome da unidade padrão do produto"),
                        fieldWithPath("name")
                                .description("Nome da unidade padrão do produto"))
                .andWithPrefix("items[].unit.",
                        fieldWithPath("id")
                                .description("Código da unidade solicitada"),
                        fieldWithPath("abbreviation")
                                .description("Abreviação do nome da unidade solicitada"),
                        fieldWithPath("name")
                                .description("Nome da unidade solicitada"));
    }

    private ResponseFieldsSnippet getResponseWithAvailableUnits() {
        return responseFields(
                fieldWithPath("id").description("Código da aprovação"),
                fieldWithPath("request")
                        .type(JsonFieldType.OBJECT)
                        .description("Solicitação de compra de forma reduzida"),
                fieldWithPath("approver")
                        .type(JsonFieldType.OBJECT)
                        .description("Funcionário o qual realizou a avaliação"),
                fieldWithPath("date")
                        .type(JsonFieldType.STRING)
                        .optional()
                        .description("Data da avaliação da aprovação. Exemplo: " + LocalDateTime.now().toString()),
                fieldWithPath("note")
                        .optional()
                        .type(JsonFieldType.STRING)
                        .description("Observação"),
                fieldWithPath("description")
                        .optional()
                        .type(JsonFieldType.STRING)
                        .description(createDescription("Descrição da solicitação, caso exista")),
                fieldWithPath("status").description("Status atual da aprovação"),
                fieldWithPath("items").description("Lista de itens solicitados"))
                .andWithPrefix("approver.",
                        fieldWithPath("id").description("Código do funcionário o qual realizou a avaliação"),
                        fieldWithPath("name").description("Nome do funcionário o qual realizou a avaliação"))
                .andWithPrefix("request.",
                        fieldWithPath("id").description("Código da solicitação"),
                        fieldWithPath("requester").description("Nome do solicitante"),
                        fieldWithPath("responsible").description("Nome do responsável indicado pelo solicitante"),
                        fieldWithPath("costCenter").description("Centro de custo"),
                        fieldWithPath("branchOffice").description("Filial associada"),
                        fieldWithPath("note").description("Observações da solicitação"),
                        fieldWithPath("reason").description("Motivo da solicitação"),
                        fieldWithPath("dateOfNeed").optional()
                                .type(JsonFieldType.STRING)
                                .description("Data de necessidade. Exemplo: " + LocalDate.now().toString()))
                .andWithPrefix("request.costCenter.",
                        fieldWithPath("id").description("Código do centro de custo"),
                        fieldWithPath("name").description("Nome"),
                        fieldWithPath("description").description("Descrição do centro de custo"))
                .andWithPrefix("request.branchOffice.",
                        fieldWithPath("id").description("Código da filial"),
                        fieldWithPath("name").description("Nome da filial"),
                        fieldWithPath("shortName").description("Nome abreviado da filial"))
                .andWithPrefix("items[].",
                        fieldWithPath("item")
                                .description("Objeto contendo as informações do item de aprovação"),
                        fieldWithPath("id")
                                .description("Código do relacionamento entre produto e a aprovação"),
                        fieldWithPath("unit")
                                .description("Unidade solicitada"),
                        fieldWithPath("quantity")
                                .description("Quantidade solicitada"),
                        fieldWithPath("status")
                                .description("Status de aprovação do produto"),
                        fieldWithPath("approval")
                                .description("Código da aprovação associada"))
                .andWithPrefix("items[].unit.",
                        fieldWithPath("id")
                                .description("Código da unidade solicitada"),
                        fieldWithPath("abbreviation")
                                .description("Abreviação do nome da unidade solicitada"),
                        fieldWithPath("name")
                                .description("Nome da unidade solicitada"))
                .andWithPrefix("items[].item.",
                        fieldWithPath("product")
                                .description("Produto solicitado"),
                        fieldWithPath("availableUnits")
                                .description("Lista com as unidades possíveis para o respectivo item"))
                .andWithPrefix("items[].item.product.",
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
                .andWithPrefix("items[].item.product.unit.",
                        fieldWithPath("id")
                                .description("Código da unidade padrão do produto"),
                        fieldWithPath("abbreviation")
                                .description("Abreviação do nome da unidade padrão do produto"),
                        fieldWithPath("name")
                                .description("Nome da unidade padrão do produto"))
                .andWithPrefix("items[].item.availableUnits[].",
                        fieldWithPath("id")
                                .description("Código da unidade solicitada"),
                        fieldWithPath("abbreviation")
                                .description("Abreviação do nome da unidade solicitada"),
                        fieldWithPath("name")
                                .description("Nome da unidade solicitada"));
    }

}
