package br.psi.giganet.api.purchase.integration.commons;

import br.psi.giganet.api.purchase.approvals.repository.ApprovalRepository;
import br.psi.giganet.api.purchase.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.common.webhooks.model.Webhook;
import br.psi.giganet.api.purchase.common.webhooks.model.WebhookServer;
import br.psi.giganet.api.purchase.common.webhooks.model.WebhookType;
import br.psi.giganet.api.purchase.config.project_property.ApplicationProperties;
import br.psi.giganet.api.purchase.config.security.repository.PermissionRepository;
import br.psi.giganet.api.purchase.cost_center.repository.CostCenterRepository;
import br.psi.giganet.api.purchase.delivery_addresses.service.DeliveryAddressesService;
import br.psi.giganet.api.purchase.employees.repository.EmployeeRepository;
import br.psi.giganet.api.purchase.integration.utils.BuilderIntegrationTest;
import br.psi.giganet.api.purchase.locations.repository.LocationRepository;
import br.psi.giganet.api.purchase.payment_conditions.repository.PaymentConditionRepository;
import br.psi.giganet.api.purchase.products.categories.repository.ProductCategoryRepository;
import br.psi.giganet.api.purchase.products.repository.ProductRepository;
import br.psi.giganet.api.purchase.projects.repository.ProjectRepository;
import br.psi.giganet.api.purchase.purchase_order.model.PurchaseOrder;
import br.psi.giganet.api.purchase.purchase_order.repository.PurchaseOrderRepository;
import br.psi.giganet.api.purchase.purchase_requests.repository.PurchaseRequestRepository;
import br.psi.giganet.api.purchase.quotation_approvals.repository.QuotationApprovalRepository;
import br.psi.giganet.api.purchase.quotations.repository.QuotationRepository;
import br.psi.giganet.api.purchase.suppliers.repository.SupplierRepository;
import br.psi.giganet.api.purchase.suppliers.taxes.repository.TaxRepository;
import br.psi.giganet.api.purchase.units.repository.UnitRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class WebhookDocs extends BuilderIntegrationTest {

    @Autowired
    private ApplicationProperties properties;

    @Autowired
    public WebhookDocs(
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
    }

    @Test
    public void webHookReceivePurchaseOrderByEntry() throws Exception {
        this.mockMvc.perform(post("/webhooks")
                .header("Signature", generateReceiveKey(WebhookServer.STOCK_API, WebhookType.STOCK_API_SAVE_ENTRY))
                .content(objectMapper.writeValueAsString(createStockApiSaveEntry()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(getWebhookRequest("Representa a categoria salva"))
                                        .andWithPrefix("data.", fieldWithPath("order").description("Ordem de compra associada"))
                                        .andWithPrefix("data.order.", getSaveEntryWebhookRequest())
                                        .andWithPrefix("data.order.items[].", getSaveEntryItemWebhookRequest())));
    }

    private FieldDescriptor[] getWebhookRequest(String... dataDescriptions) {
        return new FieldDescriptor[]{
                fieldWithPath("id").description("ID do Webhook"),
                fieldWithPath("origin").description("Emissor do webhook"),
                fieldWithPath("data")
                        .optional().type(JsonFieldType.VARIES)
                        .description(createDescription(dataDescriptions)),
                fieldWithPath("type").description(createDescription(
                        "Tipo do Evento",
                        "É composto pelo servidor de origem concatenado com o evento ocorrido"))
        };
    }

    private FieldDescriptor[] getSaveEntryWebhookRequest() {
        return new FieldDescriptor[]{
                fieldWithPath("id").description("Código ID da ordem de compra"),
                fieldWithPath("status").description("Novo status da ordem de compra"),
                fieldWithPath("items").description("Itens recebidos da ordem de compra")
        };
    }

    private FieldDescriptor[] getSaveEntryItemWebhookRequest() {
        return new FieldDescriptor[]{
                fieldWithPath("id").description("Código ID do item da ordem de compra"),
                fieldWithPath("status").description("Novo status do item")
        };
    }

    private Webhook createStockApiSaveEntry() {
        Map<String, Object> orderData = new LinkedHashMap<>();

        PurchaseOrder order = createAndSavePurchaseOrder();

        orderData.put("id", order.getId().toString());
        orderData.put("status", order.getStatus().name());
        orderData.put("items", order.getItems().stream().map(item -> {
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("id", item.getId().toString());
            itemData.put("status", ProcessStatus.RECEIVED.name());

            return itemData;
        }).collect(Collectors.toList()));

        return createStockApiSaveEntry(Collections.singletonMap("order", orderData));
    }

    private Webhook createStockApiSaveEntry(Map<String, Object> request) {
        Webhook webhook = new Webhook();
        webhook.setId(UUID.randomUUID().toString());
        webhook.setOrigin(WebhookServer.STOCK_API);
        webhook.setType(WebhookType.STOCK_API_SAVE_ENTRY);

        webhook.setData(request);

        return webhook;
    }


    private String generateSendKey(WebhookServer origin, WebhookType type) {
        return DigestUtils.sha256Hex(origin.name() + "." + type.name() + "." + properties.getWebhooks().getSecretKey());
    }

    private String generateReceiveKey(WebhookServer origin, WebhookType type) {
        if (origin.equals(WebhookServer.STOCK_API)) {
            String key = properties.getWebhooks().getStockApi().getKey();
            return DigestUtils.sha256Hex(origin.name() + "." + type.name() + "." + key);
        }
        return null;
    }

}
