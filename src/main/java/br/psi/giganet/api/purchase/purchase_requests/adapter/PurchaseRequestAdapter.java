package br.psi.giganet.api.purchase.purchase_requests.adapter;

import br.psi.giganet.api.purchase.branch_offices.adapter.BranchOfficeAdapter;
import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.config.security.model.AbstractModel;
import br.psi.giganet.api.purchase.cost_center.adapter.CostCenterAdapter;
import br.psi.giganet.api.purchase.employees.adapter.EmployeeAdapter;
import br.psi.giganet.api.purchase.products.adapter.ProductAdapter;
import br.psi.giganet.api.purchase.products.model.Product;
import br.psi.giganet.api.purchase.purchase_order.model.PurchaseOrderItem;
import br.psi.giganet.api.purchase.purchase_requests.controller.request.InsertPurchaseRequest;
import br.psi.giganet.api.purchase.purchase_requests.controller.request.UpdatePurchaseRequest;
import br.psi.giganet.api.purchase.purchase_requests.controller.response.*;
import br.psi.giganet.api.purchase.purchase_requests.model.PurchaseRequest;
import br.psi.giganet.api.purchase.purchase_requests.model.PurchaseRequestItem;
import br.psi.giganet.api.purchase.quotation_approvals.model.QuotationApproval;
import br.psi.giganet.api.purchase.quotations.model.Quotation;
import br.psi.giganet.api.purchase.quotations.model.QuotedItem;
import br.psi.giganet.api.purchase.units.adapter.UnitAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PurchaseRequestAdapter {

    @Autowired
    private UnitAdapter unitAdapter;

    @Autowired
    private EmployeeAdapter employeeAdapter;

    @Autowired
    private CostCenterAdapter costCenterAdapter;

    @Autowired
    private BranchOfficeAdapter branchOfficeAdapter;

    @Autowired
    private ProductAdapter productAdapter;

    public PurchaseRequest transform(InsertPurchaseRequest request) {
        PurchaseRequest purchaseRequest = new PurchaseRequest();
        purchaseRequest.setResponsible(employeeAdapter.create(request.getResponsible()));
        purchaseRequest.setReason(request.getReason());
        purchaseRequest.setCostCenter(costCenterAdapter.create(request.getCostCenter()));
        purchaseRequest.setBranchOffice(branchOfficeAdapter.create(request.getBranchOffice()));
        purchaseRequest.setDateOfNeed(request.getDateOfNeed() != null ?
                ZonedDateTime.parse(request.getDateOfNeed()).toLocalDate() :
                null);

        purchaseRequest.setDescription(request.getDescription());
        purchaseRequest.setNote(request.getNote());

        purchaseRequest.setItems(
                request.getProducts()
                        .stream()
                        .map(p -> {
                            PurchaseRequestItem item = new PurchaseRequestItem();
                            Product product = new Product();
                            product.setId(p.getProduct());
                            item.setProduct(product);

                            item.setQuantity(p.getQuantity());
                            item.setUnit(unitAdapter.create(p.getUnit()));

                            return item;
                        })
                        .collect(Collectors.toList())
        );

        return purchaseRequest;
    }

    public PurchaseRequest transform(UpdatePurchaseRequest request) {
        PurchaseRequest purchaseRequest = new PurchaseRequest();
        purchaseRequest.setResponsible(employeeAdapter.create(request.getResponsible()));

        purchaseRequest.setReason(request.getReason());
        purchaseRequest.setCostCenter(costCenterAdapter.create(request.getCostCenter()));
        purchaseRequest.setBranchOffice(branchOfficeAdapter.create(request.getBranchOffice()));
        purchaseRequest.setDateOfNeed(request.getDateOfNeed() != null ?
                ZonedDateTime.parse(request.getDateOfNeed()).toLocalDate() :
                null);
        purchaseRequest.setNote(request.getNote());
        purchaseRequest.setDescription(request.getDescription());

        purchaseRequest.setItems(
                request.getProducts()
                        .stream()
                        .map(p -> {
                            PurchaseRequestItem item = new PurchaseRequestItem();
                            Product product = new Product();
                            product.setId(p.getProduct());
                            item.setProduct(product);

                            item.setQuantity(p.getQuantity());
                            item.setUnit(unitAdapter.create(p.getUnit()));
                            item.setId(p.getId());

                            return item;
                        })
                        .collect(Collectors.toList())
        );

        return purchaseRequest;
    }

    @Transactional
    public PurchaseRequestProjection transform(PurchaseRequest purchaseRequest) {
        PurchaseRequestProjection projection = new PurchaseRequestProjection();
        projection.setId(purchaseRequest.getId());
        projection.setRequester(employeeAdapter.transform(purchaseRequest.getRequester()));
        projection.setDateOfNeed(purchaseRequest.getDateOfNeed());
        projection.setCreatedDate(purchaseRequest.getCreatedDate());
        projection.setStatus(purchaseRequest.getStatus());
        projection.setDescription(purchaseRequest.getDescription());
        projection.setBranchOffice(branchOfficeAdapter.transform(purchaseRequest.getBranchOffice()));

        return projection;
    }

    @Transactional
    public PurchaseRequestResponse transformToFullResponse(PurchaseRequest purchaseRequest) {
        PurchaseRequestResponse response = new PurchaseRequestResponse();
        response.setId(purchaseRequest.getId());
        response.setRequester(employeeAdapter.transform(purchaseRequest.getRequester()));
        response.setResponsible(employeeAdapter.transform(purchaseRequest.getResponsible()));
        response.setDateOfNeed(purchaseRequest.getDateOfNeed() != null ? purchaseRequest.getDateOfNeed().toString() : null);
        response.setStatus(purchaseRequest.getStatus());
        response.setNote(purchaseRequest.getNote());
        response.setDescription(purchaseRequest.getDescription());
        response.setReason(purchaseRequest.getReason());
        response.setCostCenter(costCenterAdapter.transform(purchaseRequest.getCostCenter()));
        response.setBranchOffice(branchOfficeAdapter.transform(purchaseRequest.getBranchOffice()));
        response.setProducts(
                purchaseRequest.getItems()
                        .stream()
                        .map(item -> new PurchaseRequestItemResponse(
                                item.getId(),
                                new PurchaseRequestItemProductResponse(
                                        item.getProduct().getId(),
                                        item.getProduct().getCode(),
                                        item.getProduct().getName()),
                                item.getQuantity(),
                                unitAdapter.transform(item.getUnit()),
                                item.getStatus())
                        )
                        .collect(Collectors.toList())
        );

        return response;
    }

    @Transactional
    public RequestedItemWithTraceResponse transform(PurchaseRequestItem item) {
        RequestedItemWithTraceResponse projection = new RequestedItemWithTraceResponse();

        projection.setId(item.getId());
        projection.setProduct(productAdapter.transform(item.getProduct()));
        projection.setQuantity(item.getQuantity());
        projection.setPurchaseRequest(transform(item.getPurchaseRequest()));
        projection.setStatus(item.getStatus());
        projection.setUnit(unitAdapter.transform(item.getUnit()));
        projection.setLastStage(lastPurchaseItemStageHandler(item));

        return projection;
    }

    private Map<String, Object> lastPurchaseItemStageHandler(PurchaseRequestItem item) {
        Map<String, Object> lastStage = new HashMap<>();

        if (item.getApproval() != null) {

            if (item.getApproval().getQuotedItems() != null) {

                Optional<QuotedItem> realized = item.getApproval().getQuotedItems().stream()
                        .filter(quotedItem -> quotedItem.getStatus().equals(ProcessStatus.APPROVED) || quotedItem.getStatus().equals(ProcessStatus.REALIZED))
                        .max(Comparator.comparing(AbstractModel::getCreatedDate));

                if (realized.isPresent() && realized.get().getQuotation().getApproval() != null) {

                    QuotationApproval approval = realized.get().getQuotation().getApproval();
                    if (approval.getOrders() != null && !approval.getOrders().isEmpty()) {

                        PurchaseOrderItem orderItem = approval.getOrders().stream()
                                .filter(o -> o.getSupplier().equals(realized.get().getSelectedSupplier().getSupplier()))
                                .map(o -> o.getItems().stream()
                                        .filter(i -> i.getProduct().equals(realized.get().getProduct()) &&
                                                i.getQuantity().equals(realized.get().getQuantity()) &&
                                                i.getTotal().equals(realized.get().getSelectedSupplier().getTotal()))
                                        .findFirst().orElse(new PurchaseOrderItem()))
                                .findFirst()
                                .orElse(new PurchaseOrderItem());

                        lastStage.put("stage", "PURCHASE_ORDER");
                        lastStage.put("id", orderItem.getOrder() != null ? orderItem.getOrder().getId() : null);
                        lastStage.put("lastModifiedDate", approval.getLastModifiedDate());
                        lastStage.put("status", orderItem.getStatus());

                    } else {
                        lastStage.put("stage", "QUOTATION_APPROVAL");
                        lastStage.put("id", approval.getId());
                        lastStage.put("lastModifiedDate", approval.getLastModifiedDate());
                        lastStage.put("status", approval.getEvaluation());
                    }

                } else {
                    Optional<Quotation> quotation = item.getApproval().getQuotedItems().stream()
                            .map(QuotedItem::getQuotation)
                            .max(Comparator.comparing(AbstractModel::getLastModifiedDate));

                    if (quotation.isPresent()) {
                        lastStage.put("stage", "QUOTATION");
                        lastStage.put("id", quotation.get().getId());
                        lastStage.put("lastModifiedDate", quotation.get().getLastModifiedDate());
                        lastStage.put("status", quotation.get().getStatus());

                    } else {
                        lastStage.put("stage", "PURCHASE_REQUEST_APPROVAL");
                        lastStage.put("id", item.getApproval().getApproval().getId());
                        lastStage.put("lastModifiedDate", item.getApproval().getLastModifiedDate());
                        lastStage.put("status", item.getApproval().getStatus());
                    }

                }

            } else {
                lastStage.put("stage", "PURCHASE_REQUEST_APPROVAL");
                lastStage.put("id", item.getApproval().getApproval().getId());
                lastStage.put("lastModifiedDate", item.getApproval().getLastModifiedDate());
                lastStage.put("status", item.getApproval().getStatus());
            }

        } else {
            lastStage.put("stage", "PURCHASE_REQUEST");
            lastStage.put("id", item.getPurchaseRequest().getId());
            lastStage.put("lastModifiedDate", item.getPurchaseRequest().getLastModifiedDate());
            lastStage.put("status", item.getPurchaseRequest().getStatus());
        }

        return lastStage;
    }


}
