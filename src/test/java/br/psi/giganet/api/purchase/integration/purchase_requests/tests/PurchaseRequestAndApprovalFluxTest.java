package br.psi.giganet.api.purchase.integration.purchase_requests.tests;

import br.psi.giganet.api.purchase.approvals.controller.request.ApprovalEvaluateRequest;
import br.psi.giganet.api.purchase.approvals.controller.request.ApprovalItemEvaluate;
import br.psi.giganet.api.purchase.approvals.model.Approval;
import br.psi.giganet.api.purchase.approvals.model.ApprovalItem;
import br.psi.giganet.api.purchase.approvals.repository.ApprovalRepository;
import br.psi.giganet.api.purchase.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.config.security.model.AbstractModel;
import br.psi.giganet.api.purchase.config.security.repository.PermissionRepository;
import br.psi.giganet.api.purchase.cost_center.repository.CostCenterRepository;
import br.psi.giganet.api.purchase.employees.model.Employee;
import br.psi.giganet.api.purchase.employees.repository.EmployeeRepository;
import br.psi.giganet.api.purchase.integration.utils.BuilderIntegrationTest;
import br.psi.giganet.api.purchase.integration.utils.annotations.RoleTestRoot;
import br.psi.giganet.api.purchase.products.categories.repository.ProductCategoryRepository;
import br.psi.giganet.api.purchase.products.model.Product;
import br.psi.giganet.api.purchase.products.repository.ProductRepository;
import br.psi.giganet.api.purchase.purchase_requests.controller.request.InsertPurchaseRequest;
import br.psi.giganet.api.purchase.purchase_requests.controller.request.InsertPurchaseRequestItem;
import br.psi.giganet.api.purchase.purchase_requests.controller.request.UpdatePurchaseRequest;
import br.psi.giganet.api.purchase.purchase_requests.controller.request.UpdatePurchaseRequestItem;
import br.psi.giganet.api.purchase.purchase_requests.controller.response.PurchaseRequestResponse;
import br.psi.giganet.api.purchase.purchase_requests.model.PurchaseRequest;
import br.psi.giganet.api.purchase.purchase_requests.model.PurchaseRequestItem;
import br.psi.giganet.api.purchase.purchase_requests.repository.PurchaseRequestRepository;
import br.psi.giganet.api.purchase.suppliers.repository.SupplierRepository;
import br.psi.giganet.api.purchase.suppliers.taxes.repository.TaxRepository;
import br.psi.giganet.api.purchase.units.model.Unit;
import br.psi.giganet.api.purchase.units.repository.UnitRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import javax.transaction.Transactional;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PurchaseRequestAndApprovalFluxTest extends BuilderIntegrationTest {

    @Autowired
    public PurchaseRequestAndApprovalFluxTest(
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
            BranchOfficeRepository branchOfficeRepository
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

        createCurrentUser();
        createNotificationPermissions();

    }

    /**
     * Description: Create a valid purchase request and approvals flux. This test has the propose to
     * check if the flux is okay, rejecting 2 items and approve 5 items.
     * Steps:
     * <ol>
     *     <li>Create initial data: products, cost centers and units. The purchase request will have 3 products.</li>
     *     <li>Create a purchase request using the POST endpoint. This endpoint return only a projection so, we will need to get
     *     the full body in next step</li>
     *     <li>Get the full purchase request body using GET method</li>
     *     <li>Using the full body response, set the purchase request object with data</li>
     *     <li>Send a PUT request to update data adding 2 new items. Now, the products has size 5</li>
     *     <li>Send the current request to approval. In this test, this is repeat just for check if the API will refuse correctly the second request.</li>
     *     <li>In the requests evaluation, reject 2 items and approve the others</li>
     *     <li>Create a new update request, adding 2 new items and then send to approval again</li>
     *     <li>In this time, the request evaluation is approve this 2 items remain</li>
     *     <li>Check if the expected result is correct</li>
     * </ol>
     * <br>
     * Category: VALID
     */
    @RoleTestRoot
    @Transactional
    public void partiallyApproved() throws Exception {
        // create initials datas
        for (int i = 0; i < 3; i++) {
            createAndSaveProduct();
            createAndSaveUnit();
            createAndSaveCostCenter();
        }

        PurchaseRequest purchaseRequest = new PurchaseRequest();

        // create the purchase request. the response is a projection only
        String projection = this.mockMvc.perform(post("/purchase-requests")
                .content(objectMapper.writeValueAsString(createValidInsertPurchaseRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // get full purchase request
        String response = this.mockMvc.perform(get("/purchase-requests/{id}", objectMapper.readTree(projection).get("id").asLong())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PurchaseRequestResponse responseObject = objectMapper.readValue(response, PurchaseRequestResponse.class);
        setPurchaseRequest(purchaseRequest, responseObject);

        // create a update request
        response = this.mockMvc.perform(put("/purchase-requests/{id}", purchaseRequest.getId())
                .content(objectMapper.writeValueAsString(createValidUpdatePurchaseRequest(purchaseRequest)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // get the response fields to build the complete and saved request
        PurchaseRequestResponse updateResponse = objectMapper.readValue(response, PurchaseRequestResponse.class);
        setPurchaseRequest(purchaseRequest, updateResponse);

        // send to approval
        this.mockMvc.perform(post("/purchase-requests/{id}/approvals", purchaseRequest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        // send a second time to approval -> should be rejected
        this.mockMvc.perform(post("/purchase-requests/{id}/approvals", purchaseRequest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        // get the approval and the evaluate -> reject the last 2 items
        Approval approval = approvalRepository.findByRequest(purchaseRequest).get(0);
        this.mockMvc.perform(put("/approvals/{id}/evaluate", approval.getId())
                .content(objectMapper.writeValueAsString(createValidEvaluateHandlerApproval(approval, 2)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        // get full purchase request updated from server
        response = this.mockMvc.perform(get("/purchase-requests/{id}", objectMapper.readTree(projection).get("id").asLong())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        responseObject = objectMapper.readValue(response, PurchaseRequestResponse.class);
        setPurchaseRequest(purchaseRequest, responseObject);

        // create a new update request
        response = this.mockMvc.perform(put("/purchase-requests/{id}", purchaseRequest.getId())
                .content(objectMapper.writeValueAsString(createValidUpdatePurchaseRequest(purchaseRequest)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // send to approval again
        this.mockMvc.perform(post("/purchase-requests/{id}/approvals", purchaseRequest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        // get the approval again and the evaluate
        Approval secondApproval = approvalRepository.findByRequest(purchaseRequest).stream().max(Comparator.comparing(AbstractModel::getId)).get();
        this.mockMvc.perform(put("/approvals/{id}/evaluate", secondApproval.getId())
                .content(objectMapper.writeValueAsString(createValidEvaluateHandlerApproval(secondApproval, 0)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        // check the purchase request status
        this.mockMvc.perform(get("/purchase-requests/{id}", objectMapper.readTree(projection).get("id").asLong())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.is(ProcessStatus.PARTIALLY_APPROVED.name())))
                .andExpect(jsonPath("$.products", Matchers.hasSize(7)))
                .andExpect(jsonPath("$.products[?(@.status == '" + ProcessStatus.APPROVED + "')]", Matchers.hasSize(5)))
                .andExpect(jsonPath("$.products[?(@.status == '" + ProcessStatus.REJECTED + "')]", Matchers.hasSize(2)));
    }

    /**
     * Description: Create a valid purchase request and approvals flux. This test has the propose to
     * check if the flux is okay, rejecting 4 items and approve 3 items.
     * Steps:
     * <ol>
     *     <li>Create initial data: products, cost centers and units. The purchase request will have 3 products.</li>
     *     <li>Create a purchase request using the POST endpoint. This endpoint return only a projection so, we will need to get
     *     the full body in next step</li>
     *     <li>Get the full purchase request body using GET method</li>
     *     <li>Using the full body response, set the purchase request object with data</li>
     *     <li>Send a PUT request to update data adding 2 new items. Now, the products has size 5</li>
     *     <li>Send the current request to approval. In this test, this is repeat just for check if the API will refuse correctly the second request.</li>
     *     <li>In the requests evaluation, reject 4 items and approve the others</li>
     *     <li>Create a new update request, adding 2 new items and then send to approval again</li>
     *     <li>In this time, the request evaluation is approve this 2 items remain</li>
     *     <li>Check if the expected result is correct</li>
     * </ol>
     * <br>
     * Category: VALID
     */
    @RoleTestRoot
    @Transactional
    public void partiallyRejected() throws Exception {
        // create initials datas
        for (int i = 0; i < 3; i++) {
            createAndSaveProduct();
            createAndSaveUnit();
            createAndSaveCostCenter();
        }

        PurchaseRequest purchaseRequest = new PurchaseRequest();

        // create the purchase request. the response is a projection only
        String projection = this.mockMvc.perform(post("/purchase-requests")
                .content(objectMapper.writeValueAsString(createValidInsertPurchaseRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // get full purchase request
        String response = this.mockMvc.perform(get("/purchase-requests/{id}", objectMapper.readTree(projection).get("id").asLong())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PurchaseRequestResponse responseObject = objectMapper.readValue(response, PurchaseRequestResponse.class);
        setPurchaseRequest(purchaseRequest, responseObject);

        // create a update request
        response = this.mockMvc.perform(put("/purchase-requests/{id}", purchaseRequest.getId())
                .content(objectMapper.writeValueAsString(createValidUpdatePurchaseRequest(purchaseRequest)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // get the response fields to build the complete and saved request
        PurchaseRequestResponse updateResponse = objectMapper.readValue(response, PurchaseRequestResponse.class);
        setPurchaseRequest(purchaseRequest, updateResponse);

        // send to approval
        this.mockMvc.perform(post("/purchase-requests/{id}/approvals", purchaseRequest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        // send a second time to approval -> should be rejected
        this.mockMvc.perform(post("/purchase-requests/{id}/approvals", purchaseRequest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        // get the approval and the evaluate -> reject the last 4 items
        Approval approval = approvalRepository.findByRequest(purchaseRequest).get(0);
        this.mockMvc.perform(put("/approvals/{id}/evaluate", approval.getId())
                .content(objectMapper.writeValueAsString(createValidEvaluateHandlerApproval(approval, 4)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        // get full purchase request updated from server
        response = this.mockMvc.perform(get("/purchase-requests/{id}", objectMapper.readTree(projection).get("id").asLong())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        responseObject = objectMapper.readValue(response, PurchaseRequestResponse.class);
        setPurchaseRequest(purchaseRequest, responseObject);

        // create a new update request
        response = this.mockMvc.perform(put("/purchase-requests/{id}", purchaseRequest.getId())
                .content(objectMapper.writeValueAsString(createValidUpdatePurchaseRequest(purchaseRequest)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // send to approval again
        this.mockMvc.perform(post("/purchase-requests/{id}/approvals", purchaseRequest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        // get the approval again and the evaluate
        Approval secondApproval = approvalRepository.findByRequest(purchaseRequest).stream().max(Comparator.comparing(AbstractModel::getId)).get();
        this.mockMvc.perform(put("/approvals/{id}/evaluate", secondApproval.getId())
                .content(objectMapper.writeValueAsString(createValidEvaluateHandlerApproval(secondApproval, 0)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        // check the purchase request status
        this.mockMvc.perform(get("/purchase-requests/{id}", objectMapper.readTree(projection).get("id").asLong())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.is(ProcessStatus.PARTIALLY_REJECTED.name())))
                .andExpect(jsonPath("$.products", Matchers.hasSize(7)))
                .andExpect(jsonPath("$.products[?(@.status == '" + ProcessStatus.APPROVED + "')]", Matchers.hasSize(3)))
                .andExpect(jsonPath("$.products[?(@.status == '" + ProcessStatus.REJECTED + "')]", Matchers.hasSize(4)));
    }

    @Test
    @WithMockUser(username = "teste_auto_approve@teste.com", authorities = {"ROLE_ROOT", "ROLE_ADMIN"})
    @Transactional
    public void autoApproveRequest() throws Exception {
        Employee employee = createAndSaveEmployee("teste_auto_approve@teste.com");
        employee.getPermissions().add(createAndSavePermission("ROLE_APPROVALS_WRITE"));
        employeeRepository.saveAndFlush(employee);

        // create initials datas
        for (int i = 0; i < 3; i++) {
            createAndSaveProduct();
            createAndSaveUnit();
            createAndSaveCostCenter();
        }

        PurchaseRequest purchaseRequest = new PurchaseRequest();

        // create the purchase request. the response is a projection only
        String projection = this.mockMvc.perform(post("/purchase-requests")
                .content(objectMapper.writeValueAsString(createValidInsertAutoApprovePurchaseRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // get full purchase request
        String response = this.mockMvc.perform(get("/purchase-requests/{id}", objectMapper.readTree(projection).get("id").asLong())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PurchaseRequestResponse responseObject = objectMapper.readValue(response, PurchaseRequestResponse.class);
        setPurchaseRequest(purchaseRequest, responseObject);

        // send to approval
        this.mockMvc.perform(post("/purchase-requests/{id}/approvals", purchaseRequest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.is(ProcessStatus.APPROVED.name())));
    }

    private void setPurchaseRequest(PurchaseRequest purchaseRequest, PurchaseRequestResponse response) {
        purchaseRequest.setId(response.getId());
        purchaseRequest.setResponsible(createAndSaveEmployee());
        purchaseRequest.setItems(response.getProducts()
                .stream()
                .map(i -> {
                    PurchaseRequestItem item = new PurchaseRequestItem();
                    Unit unit = new Unit();
                    unit.setId(i.getUnit().getId());
                    item.setUnit(unit);

                    Product product = new Product();
                    product.setId(i.getProduct().getId());
                    item.setProduct(product);

                    item.setQuantity(i.getQuantity());
                    item.setPurchaseRequest(purchaseRequest);
                    item.setStatus(i.getStatus());
                    item.setId(i.getId());

                    return item;
                })
                .collect(Collectors.toList()));
    }

    private InsertPurchaseRequest createValidInsertPurchaseRequest() {
        Employee responsible = createAndSaveEmployee();

        InsertPurchaseRequest request = new InsertPurchaseRequest();
        request.setResponsible(responsible.getId());
        request.setDateOfNeed(ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")).toString());
        request.setReason("Reposição de estoque");
        request.setCostCenter(createAndSaveCostCenter().getId());
        request.setBranchOffice(createAndSaveBranchOffice().getId());
        request.setProducts(
                productRepository.findAll()
                        .subList(0, 3)
                        .stream()
                        .map(product -> new InsertPurchaseRequestItem(
                                product.getId(),
                                getRandomId() * 0.15,
                                product.getUnit().getId()))
                        .collect(Collectors.toList()));
        return request;
    }

    private InsertPurchaseRequest createValidInsertAutoApprovePurchaseRequest() {
        Employee responsible = createAndSaveEmployee();

        InsertPurchaseRequest request = new InsertPurchaseRequest();
        request.setResponsible(responsible.getId());
        request.setDateOfNeed(ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")).toString());
        request.setReason("Reposição de estoque");
        request.setCostCenter(createAndSaveCostCenter().getId());
        request.setBranchOffice(createAndSaveBranchOffice().getId());
        request.setProducts(
                productRepository.findAll()
                        .subList(0, 3)
                        .stream()
                        .map(product -> new InsertPurchaseRequestItem(
                                product.getId(),
                                getRandomId() * 0.15,
                                product.getUnit().getId()))
                        .collect(Collectors.toList()));
        return request;
    }

    private UpdatePurchaseRequest createValidUpdatePurchaseRequest(PurchaseRequest purchaseRequest) {
        UpdatePurchaseRequest request = new UpdatePurchaseRequest();
        request.setResponsible(purchaseRequest.getResponsible().getId());
        request.setDateOfNeed(ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")).toString());
        request.setReason("Reposição de estoque");
        request.setCostCenter(createAndSaveCostCenter().getId());
        request.setBranchOffice(createAndSaveBranchOffice().getId());
        request.setProducts(
                purchaseRequest
                        .getItems()
                        .stream()
                        .map(item -> new UpdatePurchaseRequestItem(
                                item.getId(),
                                item.getProduct().getId(),
                                item.getQuantity() + 10,
                                item.getUnit().getId()))
                        .collect(Collectors.toList()));

        // add new products
        request.getProducts().addAll(
                productRepository.findAll()
                        .stream()
                        .sorted((p1, p2) -> p2.getId().compareTo(p1.getId()))
                        .limit(2)
                        .map(product -> new UpdatePurchaseRequestItem(
                                null, product.getId(),
                                getRandomId() * 1.5,
                                product.getUnit().getId()))
                        .collect(Collectors.toList()));
        return request;
    }

    private ApprovalEvaluateRequest createValidEvaluateHandlerApproval(Approval approvalTest, int numRejected) {
        ApprovalEvaluateRequest request = new ApprovalEvaluateRequest();
        request.setNote("Reposição de estoque " + getRandomId());
        request.setItems(new ArrayList<>());

        for (int i = 0; i < approvalTest.getItems().size(); i++) {
            ApprovalItem item = approvalTest.getItems().get(i);
            request.getItems().add(
                    new ApprovalItemEvaluate(item.getId(),
                            i >= approvalTest.getItems().size() - numRejected ? ProcessStatus.REJECTED : ProcessStatus.APPROVED));
        }
        return request;
    }

}
