package br.psi.giganet.api.purchase.integration.purchase_orders.tests;

import br.psi.giganet.api.purchase.approvals.repository.ApprovalRepository;
import br.psi.giganet.api.purchase.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.config.security.repository.PermissionRepository;
import br.psi.giganet.api.purchase.cost_center.repository.CostCenterRepository;
import br.psi.giganet.api.purchase.delivery_addresses.service.DeliveryAddressesService;
import br.psi.giganet.api.purchase.employees.model.Employee;
import br.psi.giganet.api.purchase.employees.repository.EmployeeRepository;
import br.psi.giganet.api.purchase.integration.purchase_orders.annotations.RoleTestPurchaseOrdersCompetenciesWrite;
import br.psi.giganet.api.purchase.integration.purchase_orders.annotations.RoleTestPurchaseOrdersRead;
import br.psi.giganet.api.purchase.integration.purchase_orders.annotations.RoleTestPurchaseOrdersWrite;
import br.psi.giganet.api.purchase.integration.purchase_orders.annotations.RoleTestPurchaseOrdersWriteRoot;
import br.psi.giganet.api.purchase.integration.utils.BuilderIntegrationTest;
import br.psi.giganet.api.purchase.integration.utils.RolesIntegrationTest;
import br.psi.giganet.api.purchase.integration.utils.annotations.RoleTestAdmin;
import br.psi.giganet.api.purchase.integration.utils.annotations.RoleTestRoot;
import br.psi.giganet.api.purchase.locations.repository.LocationRepository;
import br.psi.giganet.api.purchase.payment_conditions.repository.PaymentConditionRepository;
import br.psi.giganet.api.purchase.products.categories.repository.ProductCategoryRepository;
import br.psi.giganet.api.purchase.products.repository.ProductRepository;
import br.psi.giganet.api.purchase.projects.repository.ProjectRepository;
import br.psi.giganet.api.purchase.purchase_order.controller.request.*;
import br.psi.giganet.api.purchase.purchase_order.model.PurchaseOrder;
import br.psi.giganet.api.purchase.purchase_order.repository.PurchaseOrderRepository;
import br.psi.giganet.api.purchase.purchase_requests.repository.PurchaseRequestRepository;
import br.psi.giganet.api.purchase.quotation_approvals.repository.QuotationApprovalRepository;
import br.psi.giganet.api.purchase.quotations.model.enums.FreightType;
import br.psi.giganet.api.purchase.quotations.repository.QuotationRepository;
import br.psi.giganet.api.purchase.suppliers.repository.SupplierRepository;
import br.psi.giganet.api.purchase.suppliers.taxes.repository.TaxRepository;
import br.psi.giganet.api.purchase.units.repository.UnitRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PurchaseOrderTest extends BuilderIntegrationTest implements RolesIntegrationTest {

    private PurchaseOrder orderTest;

    @Autowired
    public PurchaseOrderTest(
            ProductRepository productRepository,
            SupplierRepository supplierRepository,
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository,
            PurchaseRequestRepository purchaseRequestRepository,
            ApprovalRepository approvalRepository,
            QuotationRepository quotationRepository,
            QuotationApprovalRepository quotationApprovalRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            UnitRepository unitRepository,
            ProductCategoryRepository productCategoryRepository,
            CostCenterRepository costCenterRepository,
            PaymentConditionRepository paymentConditionRepository,
            DeliveryAddressesService addressService,
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
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.costCenterRepository = costCenterRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.unitRepository = unitRepository;
        this.paymentConditionRepository = paymentConditionRepository;
        this.addressService = addressService;
        this.taxRepository = taxRepository;
        this.branchOfficeRepository = branchOfficeRepository;
        this.projectRepository = projectRepository;
        this.locationRepository = locationRepository;

        createCurrentUser();
        orderTest = createAndSavePurchaseOrder();
    }

    @RoleTestRoot
    public void findAll() throws Exception {
        this.mockMvc.perform(get("/purchase-orders")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                getPurchaseOrderProjection()));
    }

    @RoleTestRoot
    public void findAllWithoutApproval() throws Exception {
        this.mockMvc.perform(get("/purchase-orders")
                .param("withoutApproval", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestParameters(
                                        parameterWithName("withoutApproval")
                                                .description(createDescription(
                                                        "Apenas uma flag para indicar qual será o formato do retorno",
                                                        "Será retornado apenas o ID da aprovação, otimizando o tempo de resposta"))),
                                getPurchaseOrderProjectionWithoutApproval()));
    }

    @RoleTestRoot
    public void findAllWithQuotation() throws Exception {
        this.mockMvc.perform(get("/purchase-orders")
                .param("withQuotation", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestParameters(
                                        parameterWithName("withQuotation")
                                                .description(createDescription(
                                                        "Apenas uma flag para indicar qual será o formato do retorno",
                                                        "Será retornado apenas o ID da aprovação, otimizando o tempo de resposta"))),
                                getPurchaseOrderProjectionWithQuotation()));
    }

    @RoleTestRoot
    public void findAllWithQuotationAndCompetencies() throws Exception {
        this.mockMvc.perform(get("/purchase-orders")
                .param("withQuotationAndCompetencies", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestParameters(
                                        parameterWithName("withQuotationAndCompetencies")
                                                .description(createDescription(
                                                        "Apenas uma flag para indicar qual será o formato do retorno",
                                                        "Será retornado o ID da aprovação, otimizando o tempo de resposta",
                                                        "Será retornado também apenas as datas das competências encontradas"))),
                                getPurchaseOrderProjectionWithQuotationAndCompetencies()));
    }

    @RoleTestRoot
    public void findAllByAdvancedQueries() throws Exception {
        for (int i = 0; i < 2; i++) {
            createAndSavePurchaseOrder();
        }

        this.mockMvc.perform(get("/purchase-orders")
                .param("advanced", "")
                .param("page", "0")
                .param("pageSize", "5")
                .param("search",
                        ("createdDate>" + LocalDate.now().minusDays(1).toString()),
                        ("lastModifiedDate>" + LocalDate.now().minusDays(1).toString()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestParameters(
                                        parameterWithName("advanced")
                                                .description(createDescription(
                                                        "Apenas uma flag para indicar qual será o formato do retorno",
                                                        "Será retornado o ID da aprovação, otimizando o tempo de resposta",
                                                        "Será retornado também apenas o nome curto da filial, dado a necessidade atual")),
                                        parameterWithName("search").description("Lista com as pesquisas solicitadas, de acordo com a convenção"),
                                        getPagePathParameter(),
                                        getPageSizePathParameter()),
                                getAdvancedOrderProjection()));
    }

    @RoleTestRoot
    public void findById() throws Exception {
        this.mockMvc.perform(get("/purchase-orders/{id}", orderTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("id").description(createDescriptionWithNotNull("Código da ordem de compra procurada"))),
                                getPurchaseOrderResponse()));
    }

    @RoleTestRoot
    public void getPurchaseOrderReport() throws Exception {
        this.mockMvc.perform(get("/purchase-orders/reports/{id}", orderTest.getId())
                .contentType(MediaType.APPLICATION_PDF))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                pathParameters(
                                        parameterWithName("id").description(
                                                createDescriptionWithNotNull("Código da ordem de compra procurada")))));
    }

    @RoleTestRoot
    public void getPurchaseOrdersBySupplierReport() throws Exception {
        var order = createAndSavePurchaseOrder();
        this.mockMvc.perform(get("/purchase-orders/reports/suppliers/{supplier}", order.getSupplier().getId())
                .param("initialDate", LocalDate.now().minusWeeks(1).toString())
                .param("finalDate", LocalDate.now().plusWeeks(1).toString())
                .param("statuses", order.getStatus().name(), ProcessStatus.FINALIZED.name())
                .contentType(MediaType.APPLICATION_PDF))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                pathParameters(
                                        parameterWithName("supplier").description(
                                                createDescriptionWithNotNull("Código do Fornecedor desejado"))),
                                requestParameters(
                                        parameterWithName("initialDate").description("Data inicial"),
                                        parameterWithName("finalDate").description("Data final"),
                                        parameterWithName("statuses").description("Lista com os statuses procurados")
                                )));
    }

    @RoleTestRoot
    public void getPurchaseOrdersListByCompetenceReport() throws Exception {
        this.mockMvc.perform(get("/purchase-orders/reports/competencies/{competence}",
                orderTest.getCompetencies().get(0).getDate().toString())
                .param("offices", orderTest.getBranchOffice().getId().toString())
                .contentType(MediaType.APPLICATION_PDF))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                pathParameters(
                                        parameterWithName("competence").description(
                                                createDescriptionWithNotNull(
                                                        "Data da competência",
                                                        "A pesquisa conserará como data de início a competência informada, e a data de fim " +
                                                                "como sendo a soma de 1 mês a data de inicio"))),
                                requestParameters(
                                        parameterWithName("offices").description(createDescription(
                                                "Lista com os IDs das filiais a serem utilizadas pelo relatório",
                                                "Deve ser informado pelo menos 1 filial")))));
    }

    @RoleTestRoot
    public void getPurchaseOrdersListByCompetenceAndCostCenterReport() throws Exception {
        this.mockMvc.perform(get("/purchase-orders/reports/competencies/{competence}",
                orderTest.getCompetencies().get(0).getDate().toString())
                .param("costCenters", orderTest.getCostCenter().getId().toString())
                .contentType(MediaType.APPLICATION_PDF))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                pathParameters(
                                        parameterWithName("competence").description(
                                                createDescriptionWithNotNull(
                                                        "Data da competência",
                                                        "A pesquisa conserará como data de início a competência informada, e a data de fim " +
                                                                "como sendo a soma de 1 mês a data de inicio"))),
                                requestParameters(
                                        parameterWithName("costCenters").description("Centro de custos associado as ordens"),
                                        parameterWithName("office").optional().description(createDescription(
                                                "Filial desejada",
                                                "Caso seja nulo, o relatório considerará todas as filiais cadastradas")))));
    }

    @RoleTestRoot
    public void findByIdFilteringPendingItems() throws Exception {
        this.mockMvc.perform(get("/purchase-orders/{id}/pending", orderTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("id").description(createDescriptionWithNotNull("Código da ordem de compra procurada"))),
                                getPurchaseOrderResponse()));
    }

    @RoleTestRoot
    public void findLastByProduct() throws Exception {
        this.mockMvc.perform(get("/purchase-orders/products/{product}", orderTest.getItems().get(0).getProduct().getCode())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("product").description(createDescriptionWithNotNull("Código interno do produto pesquisado"))),
                                getLastPurchaseOrderItemResponse()));
    }

    @RoleTestRoot
    public void findAllItems() throws Exception {
        this.mockMvc.perform(get("/purchase-orders/items")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestParameters(
                                        parameterWithName("page")
                                                .optional()
                                                .description(createDescription(
                                                        "Número da página solicitada",
                                                        "Valor baseado em 0 (ex: pagina inicial: 0)",
                                                        "Valor default: \"0\"")),
                                        parameterWithName("pageSize")
                                                .optional()
                                                .description(createDescription(
                                                        "Tamanho da página solicitada",
                                                        "Valor default: \"100\""))),
                                getPageOrderItemProjection()));
    }

    @RoleTestRoot
    public void findAllLastItemsByQuotationApproval() throws Exception {
        this.mockMvc.perform(get("/purchase-orders/items/quotation-approvals/{approval}", orderTest.getApproval().getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("approval").description(createDescriptionWithNotNull("Código da aprovação de cotação utilizada como parâmetro"))),
                                getPurchaseOrderItemWithDetailsResponse()));
    }

    @RoleTestRoot
    public void findAllLastPurchaseByProducts() throws Exception {
        PurchaseOrder order = createAndSavePurchaseOrder();
        this.mockMvc.perform(get("/purchase-orders/items/last-purchases")
                .param("products", order.getItems().stream()
                        .limit(3)
                        .map(item -> item.getProduct().getId().toString())
                        .toArray(String[]::new))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestParameters(
                                        parameterWithName("products").description(
                                                createDescriptionWithNotEmpty("Lista com os IDs dos produtos procurados"))),
                                getPurchaseOrderItemWithDetailsResponse()));
    }

    @RoleTestRoot
    @Transactional
    public void update() throws Exception {
        this.mockMvc.perform(put("/purchase-orders/{id}", orderTest.getId())
                .content(objectMapper.writeValueAsString(createValidUpdatePurchase()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("id").description(
                                                createDescriptionWithNotNull("Código da ordem de compra a ser realizada/atualizada"))),
                                requestFields(
                                        fieldWithPath("id").description(
                                                createDescriptionWithNotNull("Código da ordem de compra")),
                                        fieldWithPath("note").description("Observações da ordem de compra"),
                                        fieldWithPath("paymentCondition").description(
                                                createDescriptionWithNotNull("Condição de pagamento ordem de compra")),
                                        fieldWithPath("freight").description(
                                                createDescriptionWithNotNull("Frete da ordem de compra")),
                                        fieldWithPath("branchOffice").description(
                                                createDescriptionWithNotNull("Filial associada a ordem de compra")),
                                        fieldWithPath("status").description(createDescription(
                                                "Status da ordem de compra",
                                                "Para este estágio, são válidos: PENDENTE, REALIZADA, EM TRANSITO e CANCELADA")),
                                        fieldWithPath("competencies").description(
                                                createDescriptionWithNotNull("Competências da ordem de compra")))
                                        .andWithPrefix("competencies[].",
                                                fieldWithPath("id").description("Código da competência"),
                                                fieldWithPath("costCenter").description("Código do centro de custo associado a esta competência"),
                                                fieldWithPath("fiscalDocument")
                                                        .optional().
                                                        type(JsonFieldType.STRING)
                                                        .description("Para este estágio, este campo é ignorado, não sendo necessário informá-lo"),
                                                fieldWithPath("date").description(createDescription(
                                                        "Data da competência",
                                                        "Competência é tratada como Mes/Ano nos processos da empresa, entretanto, para facilitar o armazenamento " +
                                                                "e manipulação, é utilizado em formato de data, considerando o dia sempre como 1 ")),
                                                fieldWithPath("total").description("Total da competência"))
                                        .andWithPrefix("freight.",
                                                fieldWithPath("id").description("Código do registro do frete"),
                                                fieldWithPath("type").description(
                                                        createDescriptionWithNotNull("Tipo do frete. FOB ou CIF")),
                                                fieldWithPath("deliveryDate").description("Data estipulada para a entrega"),
                                                fieldWithPath("address").description("Endereço de entrega"),
                                                fieldWithPath("price").description("Preço total do frete"))
                                        .andWithPrefix("freight.address.",
                                                fieldWithPath("complement").description("Complemento"),
                                                fieldWithPath("postalCode").description(
                                                        createDescriptionWithNotNull("CEP", "Deve ser informado apenas números")),
                                                fieldWithPath("street").description(
                                                        createDescriptionWithNotNull("Rua")),
                                                fieldWithPath("number").description(
                                                        createDescriptionWithNotNull("Número")),
                                                fieldWithPath("district").description(
                                                        createDescriptionWithNotNull("Bairro")),
                                                fieldWithPath("city").description(
                                                        createDescriptionWithNotNull("Cidade")),
                                                fieldWithPath("state").description(
                                                        createDescriptionWithNotNull("Estado")))
                                        .andWithPrefix("paymentCondition.",
                                                fieldWithPath("id").description(
                                                        createDescriptionWithNotNull("Código do relacionamento entre a ordem de compra e a condição de pagamento")),
                                                fieldWithPath("condition").description(
                                                        createDescriptionWithNotNull("Condição de pagamento selecionada")),
                                                fieldWithPath("dueDates").description(
                                                        createDescriptionWithNotEmpty("Datas dos vencimentos das parcelas")))
                                        .andWithPrefix("paymentCondition.dueDates[].",
                                                fieldWithPath("id").description("Código do relacionemnto entre a condição de pagamento selecionada e a data de vencimento"),
                                                fieldWithPath("dueDate").description(
                                                        createDescriptionWithNotNull("Data do vencimento"))),
                                getPurchaseOrderResponse()));
    }

    @RoleTestRoot
    @Transactional
    public void updatePurchaseOrderCompetencies() throws Exception {
        this.mockMvc.perform(put("/purchase-orders/{id}/competencies", orderTest.getId())
                .content(objectMapper.writeValueAsString(createValidUpdatePurchaseOrderCompetenciesRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("id").description(
                                                createDescriptionWithNotNull("Código da ordem de compra a ser realizada/atualizada"))),
                                requestFields(
                                        fieldWithPath("id").description(
                                                createDescriptionWithNotNull("Código da ordem de compra")),
                                        fieldWithPath("note").description("Observações da ordem de compra"),
                                        fieldWithPath("status").description(createDescription(
                                                "Status da ordem de compra",
                                                "Para este estágio, são válidos: PENDENTE, REALIZADA, EM TRANSITO, PARCIALMENTE RECEBIDA," +
                                                        "RECEBIDA e FINALIZADA"
                                        )),
                                        fieldWithPath("competencies").description(
                                                createDescriptionWithNotNull("Competências da ordem de compra")))
                                        .andWithPrefix("competencies[].",
                                                fieldWithPath("id").description("Código da competência"),
                                                fieldWithPath("costCenter").description("Código do centro de custo associado a esta competência"),
                                                fieldWithPath("fiscalDocument").description("Nota fiscal referente a respectiva competência"),
                                                fieldWithPath("date").description(createDescription(
                                                        "Data da competência",
                                                        "Competência é tratada como Mes/Ano nos processos da empresa, entretanto, para facilitar o armazenamento " +
                                                                "e manipulação, é utilizado em formato de data, considerando o dia sempre como 1 ")),
                                                fieldWithPath("total").description("Total da competência")),
                                getPurchaseOrderResponse()));
    }

    @Override
    @RoleTestPurchaseOrdersRead
    public void readAuthorized() throws Exception {
        this.mockMvc.perform(get("/purchase-orders")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/purchase-orders/{id}", orderTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/purchase-orders/{id}/pending", orderTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/purchase-orders/products/{product}", orderTest.getItems().get(0).getProduct().getCode())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/purchase-orders/items")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/purchase-orders/items/quotation-approvals/{approval}", orderTest.getApproval().getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/purchase-orders/items/last-purchases")
                .param("products", orderTest.getItems().stream()
                        .limit(3)
                        .map(item -> item.getProduct().getId().toString())
                        .toArray(String[]::new))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/purchase-orders")
                .param("withoutApproval", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/purchase-orders")
                .param("withQuotation", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/purchase-orders")
                .param("advanced", "")
                .param("page", "0")
                .param("pageSize", "5")
                .param("search",
                        ("createdDate>" + LocalDate.now().minusDays(1).toString()),
                        ("lastModifiedDate>" + LocalDate.now().minusDays(1).toString()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    @RoleTestPurchaseOrdersWrite
    public void writeAuthorized() throws Exception {
        this.mockMvc.perform(put("/purchase-orders/{id}", orderTest.getId())
                .content(objectMapper.writeValueAsString(createValidUpdatePurchase()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    @RoleTestAdmin
    public void readUnauthorized() throws Exception {
        this.mockMvc.perform(get("/purchase-orders")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/purchase-orders/{id}", orderTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/purchase-orders/{id}/pending", orderTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/purchase-orders/products/{product}", orderTest.getItems().get(0).getProduct().getCode())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/purchase-orders/items")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/purchase-orders/items/quotation-approvals/{approval}", orderTest.getApproval().getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/purchase-orders/items/last-purchases")
                .param("products", orderTest.getItems().stream()
                        .limit(3)
                        .map(item -> item.getProduct().getId().toString())
                        .toArray(String[]::new))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/purchase-orders")
                .param("withoutApproval", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/purchase-orders")
                .param("withQuotation", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/purchase-orders")
                .param("advanced", "")
                .param("page", "0")
                .param("pageSize", "5")
                .param("search",
                        ("createdDate>" + LocalDate.now().minusDays(1).toString()),
                        ("lastModifiedDate>" + LocalDate.now().minusDays(1).toString()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @Override
    @RoleTestAdmin
    public void writeUnauthorized() throws Exception {
        this.mockMvc.perform(put("/purchase-orders/{id}", orderTest.getId())
                .content(objectMapper.writeValueAsString(createValidUpdatePurchase()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @RoleTestPurchaseOrdersWriteRoot
    public void authorizedUpdateOrderAsPurchaseOrderRootPermission() throws Exception {
        Employee e = createAndSaveEmployee("teste_write_root@teste.com");
        e.getPermissions().removeIf(p -> p.getName().equalsIgnoreCase("ROLE_ROOT"));
        e.getPermissions().add(createAndSavePermission("ROLE_PURCHASE_ORDERS_WRITE_ROOT"));
        employeeRepository.save(e);

        this.mockMvc.perform(put("/purchase-orders/{id}", orderTest.getId())
                .content(objectMapper.writeValueAsString(createValidUpdatePurchase()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "teste_write_root_unauthorized@teste.com", authorities = {"ROLE_PURCHASE_ORDERS_WRITE"})
    @Transactional
    public void unauthorizedUpdateOrderAsPurchaseOrderRootPermission() throws Exception {
        Employee e = createAndSaveEmployee("teste_write_root_unauthorized@teste.com");
        e.getPermissions().removeIf(p -> p.getName().equalsIgnoreCase("ROLE_ROOT"));
        e.getPermissions().add(createAndSavePermission("ROLE_PURCHASE_ORDERS_WRITE"));
        employeeRepository.save(e);

        this.mockMvc.perform(put("/purchase-orders/{id}", orderTest.getId())
                .content(objectMapper.writeValueAsString(createValidUpdatePurchase()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @RoleTestPurchaseOrdersCompetenciesWrite
    public void authorizedUpdateOrderCompetencies() throws Exception {
        Employee e = createAndSaveEmployee("teste_competencies_write@teste.com");
        e.getPermissions().removeIf(p -> p.getName().equalsIgnoreCase("ROLE_ROOT"));
        e.getPermissions().add(createAndSavePermission("ROLE_PURCHASE_ORDERS_COMPETENCIES_WRITE"));
        employeeRepository.save(e);

        this.mockMvc.perform(put("/purchase-orders/{id}/competencies", orderTest.getId())
                .content(objectMapper.writeValueAsString(createValidUpdatePurchaseOrderCompetenciesRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "teste_competencies_write_unauthorized@teste.com", authorities = {"ROLE_PURCHASE_ORDERS_WRITE"})
    @Transactional
    public void unauthorizedUpdateOrderCompetencies() throws Exception {
        Employee e = createAndSaveEmployee("teste_competencies_write_unauthorized@teste.com");
        e.getPermissions().removeIf(p -> p.getName().equalsIgnoreCase("ROLE_ROOT"));
        e.getPermissions().add(createAndSavePermission("ROLE_PURCHASE_ORDERS_WRITE"));
        employeeRepository.save(e);

        this.mockMvc.perform(put("/purchase-orders/{id}/competencies", orderTest.getId())
                .content(objectMapper.writeValueAsString(createValidUpdatePurchaseOrderCompetenciesRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }


    @RoleTestRoot
    @Transactional
    public void managePurchaseOrderStatusUpdateFlux() throws Exception {
        PurchaseOrder order = createAndSavePurchaseOrder();

        UpdatePurchaseOrderRequest pendingRequest = createValidUpdatePurchase(order);
        pendingRequest.setStatus(ProcessStatus.PENDING);
        this.mockMvc.perform(put("/purchase-orders/{id}", order.getId())
                .content(objectMapper.writeValueAsString(pendingRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        UpdatePurchaseOrderRequest realizedRequest = createValidUpdatePurchase(order);
        realizedRequest.setStatus(ProcessStatus.REALIZED);
        this.mockMvc.perform(put("/purchase-orders/{id}", order.getId())
                .content(objectMapper.writeValueAsString(realizedRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        UpdatePurchaseOrderRequest inTransitRequest = createValidUpdatePurchase(order);
        inTransitRequest.setStatus(ProcessStatus.IN_TRANSIT);
        this.mockMvc.perform(put("/purchase-orders/{id}", order.getId())
                .content(objectMapper.writeValueAsString(inTransitRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        UpdatePurchaseOrderRequest cancelRequest = createValidUpdatePurchase(order);
        cancelRequest.setStatus(ProcessStatus.CANCELED);
        this.mockMvc.perform(put("/purchase-orders/{id}", order.getId())
                .content(objectMapper.writeValueAsString(cancelRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(put("/purchase-orders/{id}", order.getId())
                .content(objectMapper.writeValueAsString(createValidUpdatePurchase(order)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

    }

    @RoleTestRoot
    @Transactional
    public void managePurchaseOrderStatusUpdateFromCompetenciesRequestFlux() throws Exception {
        PurchaseOrder order = createAndSavePurchaseOrder();

        UpdatePurchaseOrderCompetenciesRequest partiallyReceivedRequest = createValidUpdatePurchaseOrderCompetenciesRequest(order);
        partiallyReceivedRequest.setStatus(ProcessStatus.PARTIALLY_RECEIVED);
        this.mockMvc.perform(put("/purchase-orders/{id}/competencies", order.getId())
                .content(objectMapper.writeValueAsString(partiallyReceivedRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        UpdatePurchaseOrderCompetenciesRequest receivedRequest = createValidUpdatePurchaseOrderCompetenciesRequest(order);
        receivedRequest.setStatus(ProcessStatus.RECEIVED);
        this.mockMvc.perform(put("/purchase-orders/{id}/competencies", order.getId())
                .content(objectMapper.writeValueAsString(receivedRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        UpdatePurchaseOrderCompetenciesRequest finalizedRequest = createValidUpdatePurchaseOrderCompetenciesRequest(order);
        finalizedRequest.setStatus(ProcessStatus.FINALIZED);
        this.mockMvc.perform(put("/purchase-orders/{id}/competencies", order.getId())
                .content(objectMapper.writeValueAsString(finalizedRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        // double test
        this.mockMvc.perform(put("/purchase-orders/{id}/competencies", order.getId())
                .content(objectMapper.writeValueAsString(finalizedRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        UpdatePurchaseOrderCompetenciesRequest cancelRequest = createValidUpdatePurchaseOrderCompetenciesRequest(order);
        cancelRequest.setStatus(ProcessStatus.CANCELED);
        this.mockMvc.perform(put("/purchase-orders/{id}/competencies", order.getId())
                .content(objectMapper.writeValueAsString(cancelRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        UpdatePurchaseOrderCompetenciesRequest pendingRequest = createValidUpdatePurchaseOrderCompetenciesRequest(order);
        pendingRequest.setStatus(ProcessStatus.PENDING);
        this.mockMvc.perform(put("/purchase-orders/{id}/competencies", order.getId())
                .content(objectMapper.writeValueAsString(pendingRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

    }


    private UpdatePurchaseOrderRequest createValidUpdatePurchase() {
        return createValidUpdatePurchase(orderTest);
    }

    private UpdatePurchaseOrderRequest createValidUpdatePurchase(PurchaseOrder order) {
        UpdatePurchaseOrderRequest request = new UpdatePurchaseOrderRequest();
        request.setId(order.getId());
        request.setNote("Ordem de teste");
        request.setStatus(ProcessStatus.REALIZED);
        request.setBranchOffice(order.getBranchOffice().getId());

        request.setFreight(new PurchaseOrderFreightRequest());
        request.getFreight().setId(order.getFreight().getId());
        request.getFreight().setDeliveryDate(
                ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")).plusDays(50).toString());
        request.getFreight().setType(FreightType.FOB);
        request.getFreight().setPrice(BigDecimal.TEN);
        request.getFreight().setAddress(order.getFreight().getDeliveryAddress());

        request.setPaymentCondition(new UpdatePaymentConditionRequest());
        request.getPaymentCondition().setId(order.getPaymentCondition().getId());
        request.getPaymentCondition().setCondition(createAndSavePaymentCondition().getId());
        UpdateConditionDateDueRequest dueDate = new UpdateConditionDateDueRequest();
        dueDate.setDueDate(LocalDate.now(ZoneId.of("America/Sao_Paulo")).plusMonths(3).toString());
        request.getPaymentCondition().setDueDates(Collections.singletonList(dueDate));

        request.setCompetencies(order.getCompetencies()
                .stream()
                .map(competence -> new UpdatePurchaseOrderCompetence(
                        competence.getId(),
                        competence.getDate().plusMonths(1).toString(),
                        competence.getCostCenter().getId(),
                        null,
                        competence.getTotal().add(BigDecimal.TEN)))
                .collect(Collectors.toList()));

        return request;
    }

    private UpdatePurchaseOrderCompetenciesRequest createValidUpdatePurchaseOrderCompetenciesRequest() {
        return createValidUpdatePurchaseOrderCompetenciesRequest(orderTest);
    }

    private UpdatePurchaseOrderCompetenciesRequest createValidUpdatePurchaseOrderCompetenciesRequest(PurchaseOrder order) {
        UpdatePurchaseOrderCompetenciesRequest request = new UpdatePurchaseOrderCompetenciesRequest();
        request.setId(order.getId());
        request.setStatus(ProcessStatus.FINALIZED);
        request.setNote("Observação de teste");

        request.setCompetencies(order.getCompetencies()
                .stream()
                .map(competence -> new UpdatePurchaseOrderCompetence(
                        competence.getId(),
                        competence.getDate().plusMonths(1).toString(),
                        competence.getCostCenter().getId(),
                        "NF " + getRandomId(),
                        competence.getTotal().add(BigDecimal.TEN)))
                .collect(Collectors.toList()));

        return request;
    }

    private ResponseFieldsSnippet getPurchaseOrderProjectionWithoutApproval() {
        return responseFields(fieldWithPath("[]")
                .description("Lista de todas as cotações em ordem decrescente pela data de cadastro"))
                .andWithPrefix("[].",
                        fieldWithPath("id").description("Código da ordem de compra"),
                        fieldWithPath("total").description("Valor total da ordem"),
                        fieldWithPath("createdDate").description("Data de criação ordem. Exemplo: " + ZonedDateTime.now().toString()),
                        fieldWithPath("lastModifiedDate").description("Data da ultima modificação da ordem. Exemplo: " + ZonedDateTime.now().toString()),
                        fieldWithPath("status").description("Status atual da ordem de compra"),
                        fieldWithPath("supplier").description("Fornecedor selecionado para a cotação"),
                        fieldWithPath("approval").description("Aprovação da cotação"),
                        fieldWithPath("responsible").description("Responsável pela ordem de compra"))
                .andWithPrefix("[].responsible.",
                        fieldWithPath("id").description("Código do responsável"),
                        fieldWithPath("name").description("Nome do responsável"))
                .andWithPrefix("[].supplier.",
                        fieldWithPath("id").description("Código do fornecedor"),
                        fieldWithPath("name").description("Nome do fornecedor"));
    }

    private ResponseFieldsSnippet getPurchaseOrderProjectionWithQuotation() {
        return responseFields(fieldWithPath("[]")
                .description("Lista de todas as ordens de compra decrescente pela data de cadastro"))
                .andWithPrefix("[].",
                        fieldWithPath("id").description("Código da ordem de compra"),
                        fieldWithPath("total").description("Valor total da ordem"),
                        fieldWithPath("createdDate").description("Data de criação ordem. Exemplo: " + ZonedDateTime.now().toString()),
                        fieldWithPath("lastModifiedDate").description("Data da ultima modificação da ordem. Exemplo: " + ZonedDateTime.now().toString()),
                        fieldWithPath("status").description("Status atual da ordem de compra"),
                        fieldWithPath("supplier").description("Fornecedor selecionado para a cotação"),
                        fieldWithPath("approval").description("Aprovação da cotação"),
                        fieldWithPath("quotation").description("Cotação"),
                        fieldWithPath("description").optional().type(JsonFieldType.STRING).description("Descrição da cotação"),
                        fieldWithPath("deliveryDate").optional().type(JsonFieldType.STRING).description("Data de entrega prevista para a ordem de compra"),
                        fieldWithPath("responsible").description("Responsável pela ordem de compra"))
                .andWithPrefix("[].supplier.",
                        fieldWithPath("id").description("Código do fornecedor"),
                        fieldWithPath("name").description("Nome do fornecedor"));
    }

    private ResponseFieldsSnippet getAdvancedOrderProjection() {
        return getPageContent("Lista de todas as ordens de compra decrescente pela data de cadastro")
                .andWithPrefix("content[].",
                        fieldWithPath("id").description("Código da ordem de compra"),
                        fieldWithPath("total").description("Valor total da ordem"),
                        fieldWithPath("status").description("Status atual da ordem de compra"),
                        fieldWithPath("supplier").description("Fornecedor selecionado para a cotação"),
                        fieldWithPath("approval").description("Aprovação da cotação"),
                        fieldWithPath("quotation").description("Cotação"),
                        fieldWithPath("branchOffice").description("Filial associada"),
                        fieldWithPath("description").optional().type(JsonFieldType.STRING).description("Descrição da cotação"),
                        fieldWithPath("lastModifiedDate").optional().type(JsonFieldType.STRING).description("Data da ultima modificacao da ordem de compra"),
                        fieldWithPath("responsible").description("Responsável pela ordem de compra"))
                .andWithPrefix("content[].supplier.",
                        fieldWithPath("id").description("Código do fornecedor"),
                        fieldWithPath("name").description("Nome do fornecedor"))
                .andWithPrefix("content[].branchOffice.",
                        fieldWithPath("id").optional().type(JsonFieldType.NUMBER).description("Código da filial"),
                        fieldWithPath("name").optional().type(JsonFieldType.STRING).description("Nome da filial"),
                        fieldWithPath("shortName").description("Nome abreviado da filial"))
                .andWithPrefix("content[].responsible.",
                        fieldWithPath("id").description("Código do responsável"),
                        fieldWithPath("name").description("Nome do responsável"));
    }

    private ResponseFieldsSnippet getPurchaseOrderProjectionWithQuotationAndCompetencies() {
        return responseFields(fieldWithPath("[]")
                .description("Lista de todas as ordens de compra decrescente pela data de cadastro"))
                .andWithPrefix("[].",
                        fieldWithPath("id").description("Código da ordem de compra"),
                        fieldWithPath("total").description("Valor total da ordem"),
                        fieldWithPath("competencies").description("Datas das competências das ordens de compra. Exemplo: " + LocalDate.now().toString()),
                        fieldWithPath("status").description("Status atual da ordem de compra"),
                        fieldWithPath("supplier").description("Fornecedor selecionado para a cotação"),
                        fieldWithPath("approval").description("Aprovação da cotação"),
                        fieldWithPath("quotation").description("Cotação"),
                        fieldWithPath("branchOffice").description("Filial associada"),
                        fieldWithPath("description").optional().type(JsonFieldType.STRING).description("Descrição da cotação"),
                        fieldWithPath("deliveryDate").optional().type(JsonFieldType.STRING).description("Data de entrega prevista para a ordem de compra"),
                        fieldWithPath("responsible").description("Responsável pela ordem de compra"))
                .andWithPrefix("[].supplier.",
                        fieldWithPath("id").description("Código do fornecedor"),
                        fieldWithPath("name").description("Nome do fornecedor"))
                .andWithPrefix("[].branchOffice.",
                        fieldWithPath("id").description("Código da filial"),
                        fieldWithPath("name").description("Nome da filial"),
                        fieldWithPath("shortName").description("Nome abreviado da filial"))
                .andWithPrefix("[].responsible.",
                        fieldWithPath("id").description("Código do responsável"),
                        fieldWithPath("name").description("Nome do responsável"));
    }

    private ResponseFieldsSnippet getPurchaseOrderProjection() {
        return responseFields(fieldWithPath("[]")
                .description("Lista de todas as cotações em ordem decrescente pela data de cadastro"))
                .andWithPrefix("[].",
                        fieldWithPath("id").description("Código da ordem de compra"),
                        fieldWithPath("total").description("Valor total da ordem"),
                        fieldWithPath("createdDate").description("Data de criação ordem. Exemplo: " + ZonedDateTime.now().toString()),
                        fieldWithPath("lastModifiedDate").description("Data da ultima modificação da ordem. Exemplo: " + ZonedDateTime.now().toString()),
                        fieldWithPath("status").description("Status atual da ordem de compra"),
                        fieldWithPath("supplier").description("Fornecedor selecionado para a cotação"),
                        fieldWithPath("approval").description("Aprovação da cotação"),
                        fieldWithPath("responsible").description("Responsável pela ordem de compra"))
                .andWithPrefix("[].responsible.",
                        fieldWithPath("id").description("Código do responsável"),
                        fieldWithPath("name").description("Nome do responsável"))
                .andWithPrefix("[].approval.",
                        fieldWithPath("id").description("Código da aprovação"),
                        fieldWithPath("quotation").description("Cotação avaliada"),
                        fieldWithPath("requester").description("Solicitante"),
                        fieldWithPath("responsible").optional().type(JsonFieldType.OBJECT).description("Responsável pela avaliação"),
                        fieldWithPath("date").description("Data de criação do registro. Exemplo: " + ZonedDateTime.now().toString()),
                        fieldWithPath("evaluation").description("Avaliação atual da cotação"))
                .andWithPrefix("[].approval.requester.",
                        fieldWithPath("id").description("Código do solicitante"),
                        fieldWithPath("name").description("Nome do solicitante"))
                .andWithPrefix("[].approval.responsible.",
                        fieldWithPath("id").description("Código do responsável"),
                        fieldWithPath("name").description("Nome do responsável"))
                .andWithPrefix("[].supplier.",
                        fieldWithPath("id").description("Código do fornecedor"),
                        fieldWithPath("name").description("Nome do fornecedor"))
                .andWithPrefix("[].approval.quotation.",
                        fieldWithPath("id").description("Código da solicitação de compra"),
                        fieldWithPath("note").description("Observações sobre a cotação"),
                        fieldWithPath("total").description("Valor total da cotação, incluindo o valor total de todos os itens e o valor do frete"),
                        fieldWithPath("description").optional().type(JsonFieldType.STRING).description("Descrição sobre a cotação"),
                        fieldWithPath("date").description("Data da cotação. Exemplo: " + ZonedDateTime.now().toString()),
                        fieldWithPath("responsible").description("Responsável pela cotação"),
                        fieldWithPath("branchOffice").description("Nome da filial associada"),
                        fieldWithPath("status").description("Status atual da cotação"))
                .andWithPrefix("[].approval.quotation.branchOffice.",
                        fieldWithPath("id").description("Código da filial"),
                        fieldWithPath("name").description("Nome da filial"),
                        fieldWithPath("shortName").description("Nome abreviado da filial"))
                .andWithPrefix("[].approval.quotation.responsible.",
                        fieldWithPath("id").description("Código do responsável"),
                        fieldWithPath("name").description("Nome do responsável"));
    }

    private ResponseFieldsSnippet getPurchaseOrderResponse() {
        return responseFields(
                fieldWithPath("id").description("Código da ordem de compra"),
                fieldWithPath("total").description("Valor total da ordem"),
                fieldWithPath("createdDate").description("Data de criação da ordem. Exemplo: " + ZonedDateTime.now().toString()),
                fieldWithPath("lastModifiedDate").description("Data da ultima modificação da ordem. Exemplo: " + ZonedDateTime.now().toString()),
                fieldWithPath("status").description("Status atual da ordem de compra"),
                fieldWithPath("externalLink").optional().type(JsonFieldType.STRING)
                        .description("Link para algum site externo relacionado com a ordem de compra"),
                fieldWithPath("note")
                        .type(JsonFieldType.STRING)
                        .optional()
                        .description("Observações da ordem de compra, caso existam"),
                fieldWithPath("competencies")
                        .type(JsonFieldType.ARRAY)
                        .optional()
                        .description(createDescription(
                                "Competências das ordem de compra",
                                "Este campo tornou-se obrigatório, entretanto, pode haver casos de dados legados " +
                                        "os quais não possuem este campo")),
                fieldWithPath("approval").description("Aprovação da cotação"),
                fieldWithPath("responsible").description("Responsável pela ordem de compra"),
                fieldWithPath("costCenter").description("Centro de custo principal da ordem de compra"),
                fieldWithPath("branchOffice").description("Filial associada"),
                fieldWithPath("location").optional().type(JsonFieldType.OBJECT)
                        .description("Código ID da localidade associada a cotação, caso exista"),
                fieldWithPath("project").optional().type(JsonFieldType.OBJECT)
                        .description("Código ID do projeto associado a cotação, caso exista"),
                fieldWithPath("dateOfNeed").description("Data de necessidade para os itens"),
                fieldWithPath("paymentCondition").description("Condição de pagamento"),
                fieldWithPath("freight").description("Frete referente a ordem de compra"),
                fieldWithPath("supplier").description("Fornecedor contemplado durante a cotação para os respectivos itens"),
                fieldWithPath("items").description("Itens da ordem de compra"))
                .andWithPrefix("costCenter.",
                        fieldWithPath("id").description("Código do centro de custo"),
                        fieldWithPath("name").description("Nome do centro de custo"),
                        fieldWithPath("description").description("Descrição do centro de custo"))
                .andWithPrefix("branchOffice.",
                        fieldWithPath("id").description("Código da filial"),
                        fieldWithPath("name").description("Nome da filial"),
                        fieldWithPath("shortName").description("Nome abreviado da filial"))
                .andWithPrefix("project.",
                        fieldWithPath("id").description("Código do projeto"),
                        fieldWithPath("name").description("Nome do projeto"),
                        fieldWithPath("description").description("Descrição do projeto"))
                .andWithPrefix("location.",
                        fieldWithPath("id").description("Código da localidade"),
                        fieldWithPath("name").description("Nome da localidade"),
                        fieldWithPath("description").description("Descrição da localidade"))
                .andWithPrefix("competencies[].",
                        fieldWithPath("id").description("Código da competência"),
                        fieldWithPath("costCenter").description("Centro de custo da competência"),
                        fieldWithPath("fiscalDocument")
                                .type(JsonFieldType.STRING)
                                .optional()
                                .description("Nota fiscal referente a competência, caso exista"),
                        fieldWithPath("date").description(createDescription(
                                "Data da competência",
                                "Competência é tratada como Mes/Ano nos processos da empresa, entretanto, para facilitar o armazenamento " +
                                        "e manipulação, é utilizado em formato de data, considerando o dia sempre como 1 ")),
                        fieldWithPath("total").description("Total da competência"))
                .andWithPrefix("competencies[].costCenter.",
                        fieldWithPath("id").description("Código do centro de custo"),
                        fieldWithPath("name").description("Nome do centro de custo"),
                        fieldWithPath("description").description("Descrição do centro de custo"))
                .andWithPrefix("freight.",
                        fieldWithPath("id").description("Código do registro do frete"),
                        fieldWithPath("type").description("Tipo do frete. FOB ou CIF"),
                        fieldWithPath("deliveryDate").description("Data estipulada para a entrega"),
                        fieldWithPath("deliveryAddress").description("Endereço de entrega"),
                        fieldWithPath("price").description("Preço total do frete"))
                .andWithPrefix("freight.deliveryAddress.",
                        fieldWithPath("complement").description("Complemento"),
                        fieldWithPath("postalCode").description("CEP"),
                        fieldWithPath("street").description("Rua"),
                        fieldWithPath("number").description("Número"),
                        fieldWithPath("district").description("Bairro"),
                        fieldWithPath("city").description("Cidade"),
                        fieldWithPath("state").description("Estado"))
                .andWithPrefix("paymentCondition.",
                        fieldWithPath("id").description("Código do relacionamento entre a ordem de compra e a condição de pagamento"),
                        fieldWithPath("condition").description("Condição de pagamento selecionada"),
                        fieldWithPath("dueDates").description("Datas dos vencimentos das parcelas"))
                .andWithPrefix("paymentCondition.condition.",
                        fieldWithPath("id").description("Código da condição de pagamento"),
                        fieldWithPath("name").description("Nome"),
                        fieldWithPath("numberOfInstallments").description("Número de parcelas do pagamento"),
                        fieldWithPath("daysInterval").description("Intervalo entre as parcelas, em dias"),
                        fieldWithPath("description").description("Descrição da condição de pagamento"))
                .andWithPrefix("paymentCondition.dueDates[].",
                        fieldWithPath("id").description("Código do relacionemnto entre a condição de pagamento selecionada e a data de vencimento"),
                        fieldWithPath("dueDate").description("Data do vencimento"))
                .andWithPrefix("supplier.",
                        fieldWithPath("id").description("Código do fornecedor"),
                        fieldWithPath("name").description("Nome do fornecedor"))
                .andWithPrefix("responsible.",
                        fieldWithPath("id").description("Código do responsável"),
                        fieldWithPath("name").description("Nome do responsável"))
                .andWithPrefix("approval.",
                        fieldWithPath("id").description("Código da aprovação"),
                        fieldWithPath("quotation").description("Cotação avaliada"),
                        fieldWithPath("requester").description("Solicitante"),
                        fieldWithPath("responsible").optional().type(JsonFieldType.OBJECT).description("Responsável pela avaliação"),
                        fieldWithPath("date").description("Data de criação do registro. Exemplo: " + ZonedDateTime.now().toString()),
                        fieldWithPath("evaluation").description("Avaliação atual da cotação"))
                .andWithPrefix("approval.requester.",
                        fieldWithPath("id").description("Código do solicitante"),
                        fieldWithPath("name").description("Nome do solicitante"))
                .andWithPrefix("approval.responsible.",
                        fieldWithPath("id").description("Código do responsável"),
                        fieldWithPath("name").description("Nome do responsável"))
                .andWithPrefix("approval.quotation.",
                        fieldWithPath("id").description("Código da solicitação de compra"),
                        fieldWithPath("note").description("Observações sobre a cotação"),
                        fieldWithPath("total").description("Valor total da cotação, incluindo o valor total de todos os itens e o valor do frete"),
                        fieldWithPath("description").optional().type(JsonFieldType.STRING).description("Descrição sobre a cotação"),
                        fieldWithPath("date").description("Data da cotação. Exemplo: " + ZonedDateTime.now().toString()),
                        fieldWithPath("responsible").description("Responsável pela cotação"),
                        fieldWithPath("branchOffice").description("Filial associada"),
                        fieldWithPath("status").description("Status atual da cotação"))
                .andWithPrefix("approval.quotation.branchOffice.",
                        fieldWithPath("id").description("Código da filial"),
                        fieldWithPath("name").description("Nome da filial"),
                        fieldWithPath("shortName").description("Nome abreviado da filial"))
                .andWithPrefix("approval.quotation.responsible.",
                        fieldWithPath("id").description("Código do responsável"),
                        fieldWithPath("name").description("Nome do responsável"))
                .andWithPrefix("items[].",
                        fieldWithPath("id").description("Código do relacionamento entre a ordem de compra e os produtos"),
                        fieldWithPath("product").description("Produto"),
                        fieldWithPath("supplier").description("Fornecedor selecionado para o produto"),
                        fieldWithPath("quantity").description("Quantidade para o produto"),
                        fieldWithPath("unit").description("Unidade selecionada para o produto"),
                        fieldWithPath("ipi").description("Valor do IPI, em porcentagem"),
                        fieldWithPath("icms").description("Valor do ICMS, em porcentagem"),
                        fieldWithPath("price").description("Preço unitário para o produto"),
                        fieldWithPath("discount").description("O valor do desconto em reais para o produto, caso exista"),
                        fieldWithPath("total").description("Preço total do respectivo item"),
                        fieldWithPath("status").description("Situação do item"))
                .andWithPrefix("items[].unit.",
                        fieldWithPath("id").description("Código da unidade selecionada para o produto"),
                        fieldWithPath("abbreviation").description("Abreviação do nome da unidade selecionada para o produto"),
                        fieldWithPath("name").description("Nome da unidade selecionada para o produto"))
                .andWithPrefix("items[].product.",
                        fieldWithPath("id").description("Código do produto oriundo do banco de dados"),
                        fieldWithPath("code").description("Código de identificação do produto como por exemplo código serial"),
                        fieldWithPath("name").description("Nome do produto"),
                        fieldWithPath("unit").description("Unidade padrão para o produto"),
                        fieldWithPath("manufacturer").description("Nome do fabricante do produto"))
                .andWithPrefix("items[].product.unit.",
                        fieldWithPath("id").description("Código da unidade padrão do produto"),
                        fieldWithPath("abbreviation").description("Abreviação do nome da unidade padrão do produto"),
                        fieldWithPath("name").description("Nome da unidade padrão do produto"))
                .andWithPrefix("items[].supplier.",
                        fieldWithPath("id").description("Código do fornecedor"),
                        fieldWithPath("name").description("Nome do fornecedor"));
    }

    private ResponseFieldsSnippet getLastPurchaseOrderItemResponse() {
        return responseFields(
                fieldWithPath("date").description("Data da criação da ordem do respectivo produto. Exemplo: " + ZonedDateTime.now().toString()),
                fieldWithPath("product").description("Produto"),
                fieldWithPath("supplier").description("Fornecedor selecionado na compra"),
                fieldWithPath("quantity").description("Quantidade"),
                fieldWithPath("unit").description("Unidade selecionada na compra"),
                fieldWithPath("ipi").description("Valor do IPI, em porcentagem"),
                fieldWithPath("icms").description("Valor do ICMS, em porcentagem"),
                fieldWithPath("price").description("Preço unitário para o produto"),
                fieldWithPath("discount").description("O valor do desconto em reais, caso exista"),
                fieldWithPath("total").description("Preço total do respectivo item"))
                .andWithPrefix("unit.",
                        fieldWithPath("id").description("Código da unidade selecionada para o produto"),
                        fieldWithPath("abbreviation").description("Abreviação do nome da unidade selecionada para o produto"),
                        fieldWithPath("name").description("Nome da unidade selecionada para o produto"))
                .andWithPrefix("product.",
                        fieldWithPath("id").description("Código do produto oriundo do banco de dados"),
                        fieldWithPath("code").description("Código interno de identificação do produto como por exemplo código serial"),
                        fieldWithPath("name").description("Nome do produto"),
                        fieldWithPath("unit").description("Unidade padrão para o produto"),
                        fieldWithPath("manufacturer").description("Nome do fabricante do produto"))
                .andWithPrefix("product.unit.",
                        fieldWithPath("id").description("Código da unidade padrão do produto"),
                        fieldWithPath("abbreviation").description("Abreviação do nome da unidade padrão do produto"),
                        fieldWithPath("name").description("Nome da unidade padrão do produto"))
                .andWithPrefix("supplier.",
                        fieldWithPath("id").description("Código do fornecedor"),
                        fieldWithPath("name").description("Nome do fornecedor"));
    }

    private ResponseFieldsSnippet getPurchaseOrderItemWithDetailsResponse() {
        return responseFields(
                fieldWithPath("[]").description("Lista com todos os itens encontrados"))
                .andWithPrefix("[].",
                        fieldWithPath("id").description("Código do item na ordem de compra"),
                        fieldWithPath("status").description("Situação do item na ordem de compra"),
                        fieldWithPath("date").description("Data da criação da ordem do respectivo produto. Exemplo: " + ZonedDateTime.now().toString()),
                        fieldWithPath("product").description("Produto"),
                        fieldWithPath("supplier").description("Fornecedor selecionado na compra"),
                        fieldWithPath("purchaseOrder").description("Código da ordem de compra associada"),
                        fieldWithPath("quantity").description("Quantidade"),
                        fieldWithPath("unit").description("Unidade selecionada na compra"),
                        fieldWithPath("ipi").description("Valor do IPI, em porcentagem"),
                        fieldWithPath("icms").description("Valor do ICMS, em porcentagem"),
                        fieldWithPath("price").description("Preço unitário para o produto"),
                        fieldWithPath("discount").description("O valor do desconto em reais, caso exista"),
                        fieldWithPath("total").description("Preço total do respectivo item"))
                .andWithPrefix("[].unit.",
                        fieldWithPath("id").description("Código da unidade selecionada para o produto"),
                        fieldWithPath("abbreviation").description("Abreviação do nome da unidade selecionada para o produto"),
                        fieldWithPath("name").description("Nome da unidade selecionada para o produto"))
                .andWithPrefix("[].product.",
                        fieldWithPath("id").description("Código do produto oriundo do banco de dados"),
                        fieldWithPath("code").description("Código interno de identificação do produto como por exemplo código serial"),
                        fieldWithPath("name").description("Nome do produto"),
                        fieldWithPath("unit").description("Unidade padrão para o produto"),
                        fieldWithPath("manufacturer").description("Nome do fabricante do produto"))
                .andWithPrefix("[].product.unit.",
                        fieldWithPath("id").description("Código da unidade padrão do produto"),
                        fieldWithPath("abbreviation").description("Abreviação do nome da unidade padrão do produto"),
                        fieldWithPath("name").description("Nome da unidade padrão do produto"))
                .andWithPrefix("[].supplier.",
                        fieldWithPath("id").description("Código do fornecedor"),
                        fieldWithPath("name").description("Nome do fornecedor"));
    }

    private ResponseFieldsSnippet getPageOrderItemProjection() {
        return responseFields(
                fieldWithPath("content").description("Lista com todos os itens encontrados"),
                fieldWithPath("pageable").description("Informações sobre a paginação executada"),
                fieldWithPath("totalPages").description("Número total de páginas"),
                fieldWithPath("totalElements").description("Número total de elementos"),
                fieldWithPath("last").description("Retorna 'true' se a pagina atual é a última"),
                fieldWithPath("number").description("Número da página retornada"),
                fieldWithPath("size").description("Tamanho da página atual"),
                fieldWithPath("sort").description("Informações sobre a ordenação da página"),
                fieldWithPath("numberOfElements").description("Número de elementos da página"),
                fieldWithPath("first").description("Retorna 'true' se a pagina atual é a primeira"),
                fieldWithPath("empty").description("Retorna 'true' se a pagina atual é vazia"))
                .andWithPrefix("pageable.",
                        fieldWithPath("sort").description("Retorna informações sobre a ordenação"),
                        fieldWithPath("offset").description("Retorna o offset da página atual"),
                        fieldWithPath("pageNumber").description("Retorna o número da página atual"),
                        fieldWithPath("pageSize").description("Retorna o tamanho da página solicitada"),
                        fieldWithPath("paged").description("Retorna 'true' se a paginação está sendo executada"),
                        fieldWithPath("unpaged").description("Retorna 'true' se a paginação não está sendo executada"))
                .andWithPrefix("pageable.sort.",
                        fieldWithPath("sorted").description("Retorna 'true' se a pagina atual está ordenada"),
                        fieldWithPath("unsorted").description("Retorna 'true' se a pagina atual está desordenada"),
                        fieldWithPath("empty").description("Retorna 'true' se a pagina atual é vazia"))
                .andWithPrefix("sort.",
                        fieldWithPath("sorted").description("Retorna 'true' se a pagina atual está ordenada"),
                        fieldWithPath("unsorted").description("Retorna 'true' se a pagina atual está desordenada"),
                        fieldWithPath("empty").description("Retorna 'true' se a pagina atual é vazia"))
                .andWithPrefix("content[].",
                        fieldWithPath("status").description("Situação do item na ordem de compra"),
                        fieldWithPath("createdDate").description("Data da criação da ordem do respectivo produto. Exemplo: " + ZonedDateTime.now().toString()),
                        fieldWithPath("product").description("Produto"),
                        fieldWithPath("supplier").description("Fornecedor selecionado na compra"),
                        fieldWithPath("purchaseOrder").description("Código da ordem de compra associada"),
                        fieldWithPath("price").description("Preço unitário para o produto"))
                .andWithPrefix("content[].product.",
                        fieldWithPath("code").description("Código interno de identificação do produto como por exemplo código serial"),
                        fieldWithPath("name").description("Nome do produto"))
                .andWithPrefix("content[].supplier.",
                        fieldWithPath("id").description("Código do fornecedor"),
                        fieldWithPath("name").description("Nome do fornecedor"));
    }

}
