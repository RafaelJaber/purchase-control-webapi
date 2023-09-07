package br.psi.giganet.api.purchase.integration.dashboard;

import br.psi.giganet.api.purchase.approvals.repository.ApprovalRepository;
import br.psi.giganet.api.purchase.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.config.security.repository.PermissionRepository;
import br.psi.giganet.api.purchase.cost_center.repository.CostCenterRepository;
import br.psi.giganet.api.purchase.dashboard.repository.DashboardRepositoryImpl;
import br.psi.giganet.api.purchase.delivery_addresses.service.DeliveryAddressesService;
import br.psi.giganet.api.purchase.employees.repository.EmployeeRepository;
import br.psi.giganet.api.purchase.integration.utils.BuilderIntegrationTest;
import br.psi.giganet.api.purchase.integration.utils.annotations.RoleTestAdmin;
import br.psi.giganet.api.purchase.integration.utils.annotations.RoleTestRoot;
import br.psi.giganet.api.purchase.locations.repository.LocationRepository;
import br.psi.giganet.api.purchase.payment_conditions.repository.PaymentConditionRepository;
import br.psi.giganet.api.purchase.products.categories.repository.ProductCategoryRepository;
import br.psi.giganet.api.purchase.products.repository.ProductRepository;
import br.psi.giganet.api.purchase.projects.repository.ProjectRepository;
import br.psi.giganet.api.purchase.purchase_order.repository.PurchaseOrderRepository;
import br.psi.giganet.api.purchase.purchase_requests.repository.PurchaseRequestRepository;
import br.psi.giganet.api.purchase.quotation_approvals.repository.QuotationApprovalRepository;
import br.psi.giganet.api.purchase.quotations.repository.QuotationRepository;
import br.psi.giganet.api.purchase.suppliers.repository.SupplierRepository;
import br.psi.giganet.api.purchase.suppliers.taxes.repository.TaxRepository;
import br.psi.giganet.api.purchase.units.repository.UnitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DashboardTest extends BuilderIntegrationTest {

    @MockBean
    private DashboardRepositoryImpl dashboardRepository;

    @Autowired
    public DashboardTest(
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
        this.locationRepository = locationRepository;
        this.projectRepository = projectRepository;

        createCurrentUser();

        for (int i = 0; i < 2; i++) {
            createAndSaveQuotation();
        }

        for (int i = 0; i < 2; i++) {
            createAndSavePurchaseOrder();
        }
    }

    @RoleTestAdmin
    public void getData() throws Exception {
        LocalDate initialDate = LocalDate.now().minusDays(10);
        LocalDate finalDate = LocalDate.now();
        this.setUpOrdersData(initialDate, finalDate);
        this.setUpQuotationsData(initialDate, finalDate);

        this.mockMvc.perform(get("/dashboard")
                .param("initialDate", initialDate.toString())
                .param("finalDate", finalDate.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("initialDate").description(
                                        createDescriptionWithNotNull("Data inicial a ser buscado os dados",
                                                "Deve ser informado no formato: YYYY-MM-DD")),
                                parameterWithName("finalDate").description(
                                        createDescriptionWithNotNull("Data final a ser buscado os dados",
                                                "Deve ser informado no formato: YYYY-MM-DD"))),
                        responseFields(
                                fieldWithPath("initialDate").description("Data inicial dos dados"),
                                fieldWithPath("finalDate").description("Data final dos dados"),
                                fieldWithPath("orders").description("Objeto contendo as informações de ordens de compra obtidas"),
                                fieldWithPath("quotations").description("Objeto contendo as informações de cotações obtidas"))
                                .andWithPrefix("orders.",
                                        fieldWithPath("count").description("Quantidade total de ordens de compra não canceladas"),
                                        fieldWithPath("orders").description("Lista com todas as ordens de compra não canceladas presentes no intervalo"),
                                        fieldWithPath("pendingOrders").description("Lista com todas as ordens de compra com o status PENDING presentes no intervalo"),
                                        fieldWithPath("inTransitOrders").description("Lista com todas as ordens de compra com o status IN_TRANSIT presentes no intervalo"),
                                        fieldWithPath("realizedOrders").description("Lista com todas as ordens de compra realizadas presentes no intervalo, excluindo pendentes e canceladas"),
                                        fieldWithPath("totalsRealized").description(
                                                "Valor total das ordens de compra a qual possue pelo menos uma competência no intervalo selecionado e com o status de realizadas, em reais"),
                                        fieldWithPath("totalsPending").description(
                                                "Valor total das ordens de compra a qual possue pelo menos uma competência no intervalo selecionado e com o status de pendentes, em reais"),
                                        fieldWithPath("totalsInTransit").description(
                                                "Valor total das ordens de compra a qual possue pelo menos uma competência no intervalo selecionado e com o status de em transito, em reais"),
                                        fieldWithPath("totalsByCostCenter").description(
                                                "Valor total das ordens de compra com o status de realizadas ou pendentes, agrupadas pelo centro de custo"),
                                        fieldWithPath("totalsGroupByDay").description("Valor total das ordens de compra com o status de realizadas ou pendentes, agrupadas pela data de criação da ordem"),
                                        fieldWithPath("mostPurchasedItems").description("Lista com os itens mais comprados durante o intervalo"),
                                        fieldWithPath("mostPurchasedSuppliers").description("Lista com os fornecedores com mais ordens durante o intervalo"))

                                .andWithPrefix("orders.totalsByCostCenter[].",
                                        fieldWithPath("costCenter").optional().type(JsonFieldType.STRING).description("Nome do centro de custo"),
                                        fieldWithPath("total").optional().type(JsonFieldType.NUMBER).description("Valor total associado ao respectivo centro de custo"))

                                .andWithPrefix("orders.totalsGroupByDay[].",
                                        fieldWithPath("day").description(createDescription("Dia associado", "Formato: YYYY-MM-DD")),
                                        fieldWithPath("total").description("Valor total associado ao respectivo centro de custo"))

                                .andWithPrefix("orders.mostPurchasedItems[].",
                                        fieldWithPath("item").optional().type(JsonFieldType.STRING).description("Nome do item"),
                                        fieldWithPath("countOrders").optional().type(JsonFieldType.NUMBER).description("Quantidade de ordens de compra associadas"),
                                        fieldWithPath("total").optional().type(JsonFieldType.NUMBER).description("Valor total gasto com o respectivo item em todas as ordens associadas"))

                                .andWithPrefix("orders.mostPurchasedSuppliers[].",
                                        fieldWithPath("supplier").optional().type(JsonFieldType.STRING).description("Nome do fornecedor"),
                                        fieldWithPath("countOrders").optional().type(JsonFieldType.NUMBER).description("Quantidade de ordens de compra associadas"),
                                        fieldWithPath("total").optional().type(JsonFieldType.NUMBER).description("Valor total gasto em todas as ordens associadas ao respectivo fornecedor"))

                                .andWithPrefix("orders.orders[].",
                                        fieldWithPath("id").optional().type(JsonFieldType.NUMBER).description("Código da ordem de compra"),
                                        fieldWithPath("date").optional().type(JsonFieldType.STRING).description("Data da ordem de compra"),
                                        fieldWithPath("description").optional().type(JsonFieldType.STRING).description("Descrição da cotação associada"),
                                        fieldWithPath("total").optional().type(JsonFieldType.NUMBER).description("Valor total da ordem de compra"))

                                .andWithPrefix("orders.pendingOrders[].",
                                        fieldWithPath("id").optional().type(JsonFieldType.NUMBER).description("Código da ordem de compra"),
                                        fieldWithPath("date").optional().type(JsonFieldType.STRING).description("Data da ordem de compra"),
                                        fieldWithPath("description").optional().type(JsonFieldType.STRING).description("Descrição da cotação associada"),
                                        fieldWithPath("total").optional().type(JsonFieldType.NUMBER).description("Valor total da ordem de compra"))

                                .andWithPrefix("orders.realizedOrders[].",
                                        fieldWithPath("id").optional().type(JsonFieldType.NUMBER).description("Código da ordem de compra"),
                                        fieldWithPath("date").optional().type(JsonFieldType.STRING).description("Data da ordem de compra"),
                                        fieldWithPath("description").optional().type(JsonFieldType.STRING).description("Descrição da cotação associada"),
                                        fieldWithPath("total").optional().type(JsonFieldType.NUMBER).description("Valor total da ordem de compra"))

                                .andWithPrefix("orders.inTransitOrders[].",
                                        fieldWithPath("id").optional().type(JsonFieldType.NUMBER).description("Código da ordem de compra"),
                                        fieldWithPath("date").optional().type(JsonFieldType.STRING).description("Data da ordem de compra"),
                                        fieldWithPath("description").optional().type(JsonFieldType.STRING).description("Descrição da cotação associada"),
                                        fieldWithPath("total").optional().type(JsonFieldType.NUMBER).description("Valor total da ordem de compra"))

                                .andWithPrefix("quotations.",
                                        fieldWithPath("totalsRealized").description(
                                                "Valores totais das cotações criadas no intervalo selecionado e com o status de realizadas"),
                                        fieldWithPath("totalsPending").description(
                                                "Valores totais das cotações criadas no intervalo selecionado e com o status de pendentes, em reais"),
                                        fieldWithPath("totalsApproved").description(
                                                "Valores totais das cotações criadas no intervalo selecionado e com o status de aprovadas, em reais"),
                                        fieldWithPath("totalsRejected").description(
                                                "Valores totais das cotações criadas no intervalo selecionado e com o status de rejeitadas, em reais"),
                                        fieldWithPath("totalsByCostCenter").description(
                                                "Valor total das cotações com o status de realizadas ou pendentes, agrupadas pelo centro de custo"),
                                        fieldWithPath("totalsGroupByDay").description("Valor total das cotações com o status de realizadas ou pendentes, agrupadas pela data de criação da ordem"))

                                .andWithPrefix("quotations.totalsRealized.",
                                        fieldWithPath("status").description("Status do grupo associado, em formato enum"),
                                        fieldWithPath("countQuotations").description("Quantidade de cotações associadas"),
                                        fieldWithPath("total").description("Valor total das cotações criadas no intervalo selecionado e com o status de realizadas, em reais"))

                                .andWithPrefix("quotations.totalsPending.",
                                        fieldWithPath("status").description("Status do grupo associado, em formato enum"),
                                        fieldWithPath("countQuotations").description("Quantidade de cotações associadas"),
                                        fieldWithPath("total").description("Valor total das cotações criadas no intervalo selecionado e com o status de pendente, em reais"))

                                .andWithPrefix("quotations.totalsApproved.",
                                        fieldWithPath("status").description("Status do grupo associado, em formato enum"),
                                        fieldWithPath("countQuotations").description("Quantidade de cotações associadas"),
                                        fieldWithPath("total").description("Valor total das cotações criadas no intervalo selecionado e com o status de aprovada, em reais"))

                                .andWithPrefix("quotations.totalsRejected.",
                                        fieldWithPath("status").description("Status do grupo associado, em formato enum"),
                                        fieldWithPath("countQuotations").description("Quantidade de cotações associadas"),
                                        fieldWithPath("total").description("Valor total das cotações criadas no intervalo selecionado e com o status de rejeitada, em reais"))

                                .andWithPrefix("quotations.totalsByCostCenter[].",
                                        fieldWithPath("costCenter").optional().type(JsonFieldType.STRING).description("Nome do centro de custo"),
                                        fieldWithPath("total").optional().type(JsonFieldType.NUMBER).description("Valor total associado ao respectivo centro de custo"))

                                .andWithPrefix("quotations.totalsGroupByDay[].",
                                        fieldWithPath("day").description(createDescription("Dia associado", "Formato: YYYY-MM-DD")),
                                        fieldWithPath("total").description("Valor total associado ao respectivo centro de custo"))));

    }

    @RoleTestRoot
    public void getAllPurchaseOrdersListReportAsPdf() throws Exception {
        this.mockMvc.perform(get("/dashboard/downloads/totals-orders")
                .param("initialDate", LocalDate.now().minusDays(10).toString())
                .param("finalDate", LocalDate.now().toString())
                .param("pdf", "")
                .contentType(MediaType.APPLICATION_PDF))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("pdf").description("Flag a qual indica que o retorno deve ser em PDF"),
                                parameterWithName("initialDate").description(
                                        createDescriptionWithNotNull("Data inicial a ser buscado os dados",
                                                "Deve ser informado no formato: YYYY-MM-DD")),
                                parameterWithName("finalDate").description(
                                        createDescriptionWithNotNull("Data final a ser buscado os dados",
                                                "Deve ser informado no formato: YYYY-MM-DD")))));
    }

    @RoleTestRoot
    public void getAllPurchaseOrdersListReportByStatusAsPdf() throws Exception {
        this.mockMvc.perform(get("/dashboard/downloads/orders")
                .param("initialDate", LocalDate.now().minusDays(10).toString())
                .param("finalDate", LocalDate.now().toString())
                .param("statuses", ProcessStatus.PENDING.name())
                .contentType(MediaType.APPLICATION_PDF))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("statuses").description("Lista com os status desejados"),
                                parameterWithName("initialDate").description(
                                        createDescriptionWithNotNull("Data inicial a ser buscado os dados",
                                                "Deve ser informado no formato: YYYY-MM-DD")),
                                parameterWithName("finalDate").description(
                                        createDescriptionWithNotNull("Data final a ser buscado os dados",
                                                "Deve ser informado no formato: YYYY-MM-DD")))));
    }

    @RoleTestRoot
    public void getInTransitPurchaseOrdersListReportAsPdf() throws Exception {
        this.mockMvc.perform(get("/dashboard/downloads/in-transit-orders")
                .param("initialDate", LocalDate.now().minusDays(10).toString())
                .param("finalDate", LocalDate.now().toString())
                .param("pdf", "")
                .contentType(MediaType.APPLICATION_PDF))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("pdf").description("Flag a qual indica que o retorno deve ser em PDF"),
                                parameterWithName("initialDate").description(
                                        createDescriptionWithNotNull("Data inicial a ser buscado os dados",
                                                "Deve ser informado no formato: YYYY-MM-DD")),
                                parameterWithName("finalDate").description(
                                        createDescriptionWithNotNull("Data final a ser buscado os dados",
                                                "Deve ser informado no formato: YYYY-MM-DD")))));
    }

    @RoleTestRoot
    public void getRealizedPurchaseOrdersListReportAsPdf() throws Exception {
        this.mockMvc.perform(get("/dashboard/downloads/realized-orders")
                .param("initialDate", LocalDate.now().minusDays(10).toString())
                .param("finalDate", LocalDate.now().toString())
                .param("pdf", "")
                .contentType(MediaType.APPLICATION_PDF))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("pdf").description("Flag a qual indica que o retorno deve ser em PDF"),
                                parameterWithName("initialDate").description(
                                        createDescriptionWithNotNull("Data inicial a ser buscado os dados",
                                                "Deve ser informado no formato: YYYY-MM-DD")),
                                parameterWithName("finalDate").description(
                                        createDescriptionWithNotNull("Data final a ser buscado os dados",
                                                "Deve ser informado no formato: YYYY-MM-DD")))));
    }


    @RoleTestRoot
    public void getAllPurchaseOrdersListReportAsCsv() throws Exception {
        this.mockMvc.perform(get("/dashboard/downloads/totals-orders")
                .param("initialDate", LocalDate.now().minusDays(10).toString())
                .param("finalDate", LocalDate.now().toString())
                .param("csv", "")
                .contentType("text/csv"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("csv").description("Flag a qual indica que o retorno deve ser em CSV"),
                                parameterWithName("initialDate").description(
                                        createDescriptionWithNotNull("Data inicial a ser buscado os dados",
                                                "Deve ser informado no formato: YYYY-MM-DD")),
                                parameterWithName("finalDate").description(
                                        createDescriptionWithNotNull("Data final a ser buscado os dados",
                                                "Deve ser informado no formato: YYYY-MM-DD")))));
    }

    @RoleTestRoot
    public void getInTransitPurchaseOrdersListReportAsCsv() throws Exception {
        this.mockMvc.perform(get("/dashboard/downloads/in-transit-orders")
                .param("initialDate", LocalDate.now().minusDays(10).toString())
                .param("finalDate", LocalDate.now().toString())
                .param("csv", "")
                .contentType("text/csv"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("csv").description("Flag a qual indica que o retorno deve ser em CSV"),
                                parameterWithName("initialDate").description(
                                        createDescriptionWithNotNull("Data inicial a ser buscado os dados",
                                                "Deve ser informado no formato: YYYY-MM-DD")),
                                parameterWithName("finalDate").description(
                                        createDescriptionWithNotNull("Data final a ser buscado os dados",
                                                "Deve ser informado no formato: YYYY-MM-DD")))));
    }

    @RoleTestRoot
    public void getRealizedPurchaseOrdersListReportAsCsv() throws Exception {
        this.mockMvc.perform(get("/dashboard/downloads/realized-orders")
                .param("initialDate", LocalDate.now().minusDays(10).toString())
                .param("finalDate", LocalDate.now().toString())
                .param("csv", "")
                .contentType("text/csv"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("csv").description("Flag a qual indica que o retorno deve ser em CSV"),
                                parameterWithName("initialDate").description(
                                        createDescriptionWithNotNull("Data inicial a ser buscado os dados",
                                                "Deve ser informado no formato: YYYY-MM-DD")),
                                parameterWithName("finalDate").description(
                                        createDescriptionWithNotNull("Data final a ser buscado os dados",
                                                "Deve ser informado no formato: YYYY-MM-DD")))));
    }

    private void setUpOrdersData(LocalDate initialDate, LocalDate finalDate) {
        when(dashboardRepository.countOrdersByStatusNotInAndCompetence(initialDate, finalDate, ProcessStatus.CANCELED))
                .thenReturn(BigInteger.valueOf(100));

        List<Map<String, Object>> ordersMockList = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> order = new HashMap<>();
            order.put("id", 1L);
            order.put("date", initialDate.plusDays(i).toString());
            order.put("description", "Descrição da ordem");
            order.put("total", BigDecimal.TEN.multiply(BigDecimal.valueOf(i)));
            ordersMockList.add(order);
        }
        when(dashboardRepository.findAllPurchaseOrdersByCompetence(initialDate, finalDate,
                ProcessStatus.PENDING, ProcessStatus.REALIZED, ProcessStatus.IN_TRANSIT,
                ProcessStatus.PARTIALLY_RECEIVED, ProcessStatus.RECEIVED, ProcessStatus.FINALIZED))
                .thenReturn(ordersMockList);

        when(dashboardRepository.findAllPurchaseOrdersByCompetence(initialDate, finalDate,
                ProcessStatus.REALIZED, ProcessStatus.IN_TRANSIT,
                ProcessStatus.PARTIALLY_RECEIVED, ProcessStatus.RECEIVED, ProcessStatus.FINALIZED))
                .thenReturn(ordersMockList);

        when(dashboardRepository.findAllPurchaseOrdersByCompetence(initialDate, finalDate, ProcessStatus.PENDING))
                .thenReturn(ordersMockList);

        when(dashboardRepository.findAllPurchaseOrdersByCompetence(initialDate, finalDate, ProcessStatus.IN_TRANSIT))
                .thenReturn(ordersMockList);

        List<Map<String, Object>> costCenterMockList = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> order = new HashMap<>();
            order.put("costCenter", "Centro de Custo " + i);
            order.put("total", BigDecimal.TEN.multiply(BigDecimal.valueOf(i)));
            costCenterMockList.add(order);
        }
        when(dashboardRepository.totalOrdersByStatusGroupByCostCenterAndCompetence(initialDate, finalDate,
                ProcessStatus.PENDING, ProcessStatus.REALIZED, ProcessStatus.IN_TRANSIT,
                ProcessStatus.PARTIALLY_RECEIVED, ProcessStatus.RECEIVED, ProcessStatus.FINALIZED))
                .thenReturn(costCenterMockList);

        List<Map<String, Object>> ordersByDaysMockList = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> order = new HashMap<>();
            order.put("day", initialDate.plusDays(1).toString());
            order.put("total", BigDecimal.TEN.multiply(BigDecimal.valueOf(i)));
            ordersByDaysMockList.add(order);
        }
        when(dashboardRepository.totalOrdersByStatusGroupByDays(initialDate, finalDate,
                ProcessStatus.PENDING, ProcessStatus.REALIZED, ProcessStatus.IN_TRANSIT,
                ProcessStatus.PARTIALLY_RECEIVED, ProcessStatus.RECEIVED, ProcessStatus.FINALIZED))
                .thenReturn(ordersByDaysMockList);

        when(dashboardRepository.totalOrdersByStatusByCompetence(initialDate, finalDate, ProcessStatus.PENDING))
                .thenReturn(BigDecimal.TEN);

        when(dashboardRepository.totalOrdersByStatusByCompetence(initialDate, finalDate, ProcessStatus.IN_TRANSIT))
                .thenReturn(BigDecimal.TEN);

        when(dashboardRepository.totalOrdersByStatusByCompetence(initialDate, finalDate,
                ProcessStatus.REALIZED, ProcessStatus.IN_TRANSIT,
                ProcessStatus.PARTIALLY_RECEIVED, ProcessStatus.RECEIVED, ProcessStatus.FINALIZED))
                .thenReturn(BigDecimal.TEN);

        List<Map<String, Object>> itemsMockList = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> order = new HashMap<>();
            order.put("item", "Item " + i);
            order.put("countOrders", i);
            order.put("total", BigDecimal.TEN.multiply(BigDecimal.valueOf(i)));
            itemsMockList.add(order);
        }
        when(dashboardRepository.findMostPurchasedItemsByCompetence(initialDate, finalDate))
                .thenReturn(itemsMockList);

        List<Map<String, Object>> suppliersMockList = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> order = new HashMap<>();
            order.put("supplier", "Fornecedor " + i);
            order.put("countOrders", i);
            order.put("total", BigDecimal.TEN.multiply(BigDecimal.valueOf(i)));
            suppliersMockList.add(order);
        }
        when(dashboardRepository.findMostPurchasedSuppliersByCompetence(initialDate, finalDate))
                .thenReturn(suppliersMockList);
    }

    private void setUpQuotationsData(LocalDate initialDate, LocalDate finalDate) {

        Map<String, Object> approvedMockMap = new HashMap<>();
        approvedMockMap.put("status", ProcessStatus.APPROVED.name());
        approvedMockMap.put("countQuotations", 1);
        approvedMockMap.put("total", BigDecimal.TEN.multiply(BigDecimal.valueOf(1)));
        when(dashboardRepository.countAndTotalsQuotations(initialDate, finalDate, ProcessStatus.APPROVED))
                .thenReturn(approvedMockMap);

        Map<String, Object> pendingMockMap = new HashMap<>();
        pendingMockMap.put("status", ProcessStatus.PENDING.name());
        pendingMockMap.put("countQuotations", 1);
        pendingMockMap.put("total", BigDecimal.TEN.multiply(BigDecimal.valueOf(1)));
        when(dashboardRepository.countAndTotalsQuotations(initialDate, finalDate, ProcessStatus.PENDING))
                .thenReturn(pendingMockMap);

        Map<String, Object> realizedMockMap = new HashMap<>();
        realizedMockMap.put("status", ProcessStatus.REALIZED.name());
        realizedMockMap.put("countQuotations", 1);
        realizedMockMap.put("total", BigDecimal.TEN.multiply(BigDecimal.valueOf(1)));
        when(dashboardRepository.countAndTotalsQuotations(initialDate, finalDate, ProcessStatus.REALIZED))
                .thenReturn(realizedMockMap);

        Map<String, Object> rejectMockMap = new HashMap<>();
        rejectMockMap.put("status", ProcessStatus.REJECTED.name());
        rejectMockMap.put("countQuotations", 1);
        rejectMockMap.put("total", BigDecimal.TEN.multiply(BigDecimal.valueOf(1)));
        when(dashboardRepository.countAndTotalsQuotations(initialDate, finalDate, ProcessStatus.REJECTED))
                .thenReturn(rejectMockMap);

        List<Map<String, Object>> costCenterMockList = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> order = new HashMap<>();
            order.put("costCenter", "Centro de Custo " + i);
            order.put("total", BigDecimal.TEN.multiply(BigDecimal.valueOf(i)));
            costCenterMockList.add(order);
        }
        when(dashboardRepository.totalQuotationsByStatusGroupByCostCenter(initialDate, finalDate,
                ProcessStatus.REALIZED, ProcessStatus.PENDING, ProcessStatus.APPROVED))
                .thenReturn(costCenterMockList);

        List<Map<String, Object>> ordersByDaysMockList = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> order = new HashMap<>();
            order.put("day", initialDate.plusDays(1).toString());
            order.put("total", BigDecimal.TEN.multiply(BigDecimal.valueOf(i)));
            ordersByDaysMockList.add(order);
        }
        when(dashboardRepository.totalQuotationsByStatusGroupByDays(initialDate, finalDate,
                ProcessStatus.REALIZED, ProcessStatus.PENDING, ProcessStatus.APPROVED))
                .thenReturn(ordersByDaysMockList);
    }
}
