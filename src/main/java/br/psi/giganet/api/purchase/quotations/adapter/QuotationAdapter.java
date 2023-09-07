package br.psi.giganet.api.purchase.quotations.adapter;

import br.psi.giganet.api.purchase.approvals.adapter.ApprovalAdapter;
import br.psi.giganet.api.purchase.branch_offices.adapter.BranchOfficeAdapter;
import br.psi.giganet.api.purchase.cost_center.adapter.CostCenterAdapter;
import br.psi.giganet.api.purchase.employees.adapter.EmployeeAdapter;
import br.psi.giganet.api.purchase.locations.adapter.LocationAdapter;
import br.psi.giganet.api.purchase.payment_conditions.adapter.PaymentConditionAdapter;
import br.psi.giganet.api.purchase.products.adapter.ProductAdapter;
import br.psi.giganet.api.purchase.projects.adapter.ProjectAdapter;
import br.psi.giganet.api.purchase.purchase_order.model.PurchaseOrderItem;
import br.psi.giganet.api.purchase.quotation_approvals.model.QuotationApproval;
import br.psi.giganet.api.purchase.quotations.controller.request.InsertQuotationRequest;
import br.psi.giganet.api.purchase.quotations.controller.request.UpdateQuotationRequest;
import br.psi.giganet.api.purchase.quotations.controller.response.*;
import br.psi.giganet.api.purchase.quotations.model.*;
import br.psi.giganet.api.purchase.quotations.repository.QuotationEagerProjection;
import br.psi.giganet.api.purchase.suppliers.adapter.SupplierAdapter;
import br.psi.giganet.api.purchase.units.adapter.UnitAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class QuotationAdapter {

    @Autowired
    private ProductAdapter productAdapter;

    @Autowired
    private SupplierAdapter supplierAdapter;

    @Autowired
    private ApprovalAdapter approvalAdapter;

    @Autowired
    private EmployeeAdapter employeeAdapter;

    @Autowired
    private UnitAdapter unitAdapter;

    @Autowired
    private CostCenterAdapter costCenterAdapter;

    @Autowired
    private BranchOfficeAdapter branchOfficeAdapter;

    @Autowired
    private PaymentConditionAdapter paymentConditionAdapter;

    @Autowired
    private ProjectAdapter projectAdapter;

    @Autowired
    private LocationAdapter locationAdapter;

    public Quotation create(final Long id) {
        final Quotation quotation = new Quotation();
        quotation.setId(id);
        return quotation;
    }

    public Quotation transform(final InsertQuotationRequest request) {
        final Quotation quotation = new Quotation();
        quotation.setNote(request.getNote());
        quotation.setFreight(new QuotationFreight());
        quotation.getFreight().setType(request.getFreight().getType());
        quotation.getFreight().setPrice(request.getFreight().getPrice());
        quotation.setCostCenter(costCenterAdapter.create(request.getCostCenter()));
        quotation.setBranchOffice(branchOfficeAdapter.create(request.getBranchOffice()));
        quotation.setDateOfNeed(request.getDateOfNeed() != null ?
                LocalDate.parse(request.getDateOfNeed()) : null);
        quotation.setDescription(request.getDescription());
        quotation.setExternalLink(request.getExternalLink());

        quotation.setPaymentCondition(new QuotationPaymentCondition());
        quotation.getPaymentCondition().setQuotation(quotation);
        quotation.getPaymentCondition().setCondition(
                paymentConditionAdapter.create(request.getPaymentCondition().getCondition()));
        quotation.getPaymentCondition().setDueDates(
                request.getPaymentCondition().getDueDates()
                        .stream()
                        .map(dueDate -> {
                            QuotationConditionDueDate conditionDueDate = new QuotationConditionDueDate();
                            conditionDueDate.setCondition(quotation.getPaymentCondition());
                            conditionDueDate.setDueDate(dueDate.getDueDate() != null ?
                                    LocalDate.parse(dueDate.getDueDate()) : null);

                            return conditionDueDate;
                        })
                        .collect(Collectors.toList())
        );

        quotation.setLocation(request.getLocation() == null ? null : locationAdapter.create(request.getLocation()));
        quotation.setProject(request.getProject() == null ? null : projectAdapter.create(request.getProject()));

        quotation.setItems(
                request.getProducts()
                        .stream()
                        .map(product -> {
                            final QuotedItem item = new QuotedItem();
                            item.setApprovedItem(approvalAdapter.createItem(product.getApprovalItem()));
                            item.setQuantity(product.getQuantity());
                            item.setUnit(unitAdapter.create(product.getUnit()));
                            item.setProduct(productAdapter.create(product.getCode()));
                            item.setSuppliers(
                                    product.getSuppliers()
                                            .stream()
                                            .map(supplier -> {
                                                final SupplierItemQuotation quotedItem = new SupplierItemQuotation();
                                                quotedItem.setSupplier(supplierAdapter.create(supplier.getSupplierId()));
                                                quotedItem.setIsSelected(supplier.getIsSelected());
                                                quotedItem.setQuantity(supplier.getQuantity());
                                                quotedItem.setUnit(unitAdapter.create(supplier.getUnit()));
                                                quotedItem.setPrice(supplier.getPrice());
                                                quotedItem.setIpi(supplier.getIpi());
                                                quotedItem.setIcms(supplier.getIcms());
                                                quotedItem.setDiscount(supplier.getDiscount());
                                                quotedItem.setTotal(supplier.getTotal());
                                                return quotedItem;
                                            })
                                            .collect(Collectors.toList())
                            );
                            return item;
                        })
                        .collect(Collectors.toList())
        );

        return quotation;
    }

    public Quotation transform(final UpdateQuotationRequest request) {
        final Quotation quotation = new Quotation();
        quotation.setId(request.getId());
        quotation.setNote(request.getNote());
        quotation.setFreight(new QuotationFreight());
        quotation.getFreight().setType(request.getFreight().getType());
        quotation.getFreight().setPrice(request.getFreight().getPrice());
        quotation.setCostCenter(costCenterAdapter.create(request.getCostCenter()));
        quotation.setBranchOffice(branchOfficeAdapter.create(request.getBranchOffice()));
        quotation.setDateOfNeed(request.getDateOfNeed() != null ?
                LocalDate.parse(request.getDateOfNeed()) : null);
        quotation.setDescription(request.getDescription());
        quotation.setExternalLink(request.getExternalLink());

        quotation.setPaymentCondition(new QuotationPaymentCondition());
        quotation.getPaymentCondition().setId(request.getPaymentCondition().getId());
        quotation.getPaymentCondition().setQuotation(quotation);
        quotation.getPaymentCondition().setCondition(
                paymentConditionAdapter.create(request.getPaymentCondition().getCondition()));
        quotation.getPaymentCondition().setDueDates(
                request.getPaymentCondition().getDueDates()
                        .stream()
                        .map(dueDate -> {
                            QuotationConditionDueDate conditionDueDate = new QuotationConditionDueDate();
                            conditionDueDate.setId(dueDate.getId());
                            conditionDueDate.setCondition(quotation.getPaymentCondition());
                            conditionDueDate.setDueDate(dueDate.getDueDate() != null ?
                                    LocalDate.parse(dueDate.getDueDate()) : null);

                            return conditionDueDate;
                        })
                        .collect(Collectors.toList())
        );

        quotation.setLocation(request.getLocation() == null ? null : locationAdapter.create(request.getLocation()));
        quotation.setProject(request.getProject() == null ? null : projectAdapter.create(request.getProject()));

        quotation.setItems(
                request.getProducts()
                        .stream()
                        .map(product -> {
                            final QuotedItem item = new QuotedItem();
                            item.setId(product.getId());
                            item.setApprovedItem(approvalAdapter.createItem(product.getApprovalItem()));
                            item.setQuantity(product.getQuantity());
                            item.setUnit(unitAdapter.create(product.getUnit()));
                            item.setProduct(productAdapter.create(product.getCode()));
                            item.setSuppliers(
                                    product.getSuppliers()
                                            .stream()
                                            .map(supplier -> {
                                                final SupplierItemQuotation quotedItem = new SupplierItemQuotation();
                                                quotedItem.setId(supplier.getId());
                                                quotedItem.setSupplier(supplierAdapter.create(supplier.getSupplierId()));
                                                quotedItem.setIsSelected(supplier.getIsSelected());
                                                quotedItem.setQuantity(supplier.getQuantity());
                                                quotedItem.setUnit(unitAdapter.create(supplier.getUnit()));
                                                quotedItem.setPrice(supplier.getPrice());
                                                quotedItem.setIpi(supplier.getIpi());
                                                quotedItem.setIcms(supplier.getIcms());
                                                quotedItem.setDiscount(supplier.getDiscount());
                                                quotedItem.setTotal(supplier.getTotal());
                                                return quotedItem;
                                            })
                                            .collect(Collectors.toList())
                            );
                            return item;
                        })
                        .collect(Collectors.toList())
        );

        return quotation;
    }

    @Transactional
    public QuotationProjection transform(final Quotation quotation) {
        final QuotationProjection projection = new QuotationProjection();
        projection.setId(quotation.getId());
        projection.setNote(quotation.getNote());
        projection.setStatus(quotation.getStatus());
        projection.setDate(quotation.getCreatedDate());
        projection.setDescription(quotation.getDescription());
        projection.setTotal(quotation.getTotal());
        projection.setResponsible(employeeAdapter.transform(quotation.getResponsible()));
        projection.setBranchOffice(branchOfficeAdapter.transform(quotation.getBranchOffice()));

        return projection;
    }

    @Transactional
    public QuotationProjection transform(final QuotationEagerProjection quotation) {
        final QuotationProjection projection = new QuotationProjection();
        projection.setId(quotation.getId());
        projection.setStatus(quotation.getStatus());
        projection.setDate(quotation.getCreatedDate());
        projection.setDescription(quotation.getDescription());
        projection.setTotal(quotation.getTotal());
        projection.setBranchOffice(branchOfficeAdapter.transform(
                quotation.getBranchOfficeId(),
                quotation.getBranchOfficeName(),
                quotation.getBranchOfficeShortName()));
        projection.setResponsible(employeeAdapter.transform(quotation.getResponsibleId(), quotation.getResponsibleName()));

        return projection;
    }

    @Transactional
    public SupplierItemQuotationResponse transform(final SupplierItemQuotation quotedItem) {
        final SupplierItemQuotationResponse response = new SupplierItemQuotationResponse();
        response.setId(quotedItem.getId());
        response.setPrice(quotedItem.getPrice());
        response.setQuantity(quotedItem.getQuantity());
        response.setUnit(unitAdapter.transform(quotedItem.getUnit()));
        response.setIpi(quotedItem.getIpi());
        response.setIcms(quotedItem.getIcms());
        response.setDiscount(quotedItem.getDiscount());
        response.setSupplier(supplierAdapter.transform(quotedItem.getSupplier()));

        return response;
    }

    @Transactional
    public LastQuotedItemResponse transformToLastQuotedItemResponse(final SupplierItemQuotation quotedItem) {
        final LastQuotedItemResponse response = new LastQuotedItemResponse();
        response.setId(quotedItem.getId());
        response.setPrice(quotedItem.getPrice());
        response.setQuantity(quotedItem.getQuantity());
        response.setUnit(unitAdapter.transform(quotedItem.getUnit()));
        response.setIpi(quotedItem.getIpi());
        response.setIcms(quotedItem.getIcms());
        response.setDiscount(quotedItem.getDiscount());
        response.setSupplier(supplierAdapter.transform(quotedItem.getSupplier()));

        return response;
    }

    @Transactional
    public QuotationSuppliersResponse transformToQuotationSuppliersResponse(final Quotation quotation) {
        QuotationSuppliersResponse response = new QuotationSuppliersResponse();
        response.setId(quotation.getId());
        response.setSuppliers(new HashSet<>());
        response.setSuppliers(
                quotation.getItems().stream()
                        .map(item ->
                                item.getSuppliers().stream()
                                        .map(s -> supplierAdapter.transformProjectionAndEmail(s.getSupplier()))
                                        .collect(Collectors.toSet()))
                        .reduce((result, element) -> {
                            result.addAll(element);
                            return result;
                        })
                        .orElse(new HashSet<>()));

        return response;
    }

    @Transactional
    public QuotationResponse transformToFullResponse(final Quotation quotation) {
        return transformToFullResponse(quotation, QuotedItemType.DEFAULT);
    }

    @Transactional
    public QuotationResponse transformToFullResponse(final Quotation quotation, QuotedItemType responseType) {
        final QuotationResponse response = new QuotationResponse();
        response.setId(quotation.getId());
        response.setNote(quotation.getNote());
        response.setTotal(quotation.getTotal());
        response.setStatus(quotation.getStatus());
        response.setDate(quotation.getCreatedDate());
        response.setDateOfNeed(quotation.getDateOfNeed());
        response.setResponsible(employeeAdapter.transform(quotation.getResponsible()));
        response.setCostCenter(costCenterAdapter.transform(quotation.getCostCenter()));
        response.setBranchOffice(quotation.getBranchOffice() != null ? branchOfficeAdapter.transform(quotation.getBranchOffice()) : null);
        response.setLocation(quotation.getLocation() != null ? locationAdapter.transform(quotation.getLocation()) : null);
        response.setProject(quotation.getProject() != null ? projectAdapter.transform(quotation.getProject()) : null);
        response.setFreight(
                new QuotationFreightResponse(
                        quotation.getFreight() != null ? quotation.getFreight().getType() : null,
                        quotation.getFreight() != null ? quotation.getFreight().getPrice() : null));
        response.setDescription(quotation.getDescription());
        response.setExternalLink(quotation.getExternalLink());

        response.setPaymentCondition(new QuotationPaymentConditionResponse());
        response.getPaymentCondition().setId(quotation.getPaymentCondition().getId());
        response.getPaymentCondition()
                .setCondition(paymentConditionAdapter.transform(
                        quotation.getPaymentCondition().getCondition()));
        response.getPaymentCondition()
                .setDueDates(quotation.getPaymentCondition()
                        .getDueDates()
                        .stream()
                        .map(dueDate -> {
                            ConditionDueDateResponse dueDateResponse = new ConditionDueDateResponse();
                            dueDateResponse.setId(dueDate.getId());
                            dueDateResponse.setDueDate(dueDate.getDueDate());
                            return dueDateResponse;
                        })
                        .collect(Collectors.toList()));

        response.setItems(
                quotation.getItems()
                        .stream()
                        .map(item -> transform(item, responseType))
                        .collect(Collectors.toList())
        );

        return response;
    }

    @SuppressWarnings("ConstantConditions")
    public AbstractQuotedItemResponse transform(QuotedItem item, QuotedItemType responseType) {
        final AbstractQuotedItemResponse i;
        switch (responseType) {
            case DEFAULT:
                i = new QuotedItemResponse();
                break;
            case WITH_TRACE:
                i = new QuotedItemWithApprovalTraceResponse();
                break;
            case WITH_AVAILABLE_UNITS:
                i = new QuotedItemWithAvailableUnitsResponse();
                break;
            default:
                return null;
        }

        i.setQuantity(item.getQuantity());
        i.setUnit(unitAdapter.transform(item.getUnit()));
        i.setId(item.getId());

        if (responseType == QuotedItemType.WITH_TRACE) {
            if (item.getApprovedItem() != null) {
                ((QuotedItemWithApprovalTraceResponse) i).setApprovalItem(
                        approvalAdapter.transformWithTrace(item.getApprovedItem()));
            }
            ((QuotedItemWithApprovalTraceResponse) i).setProduct(productAdapter.transform(item.getProduct()));

        } else if (responseType == QuotedItemType.DEFAULT) {
            if (item.getApprovedItem() != null) {
                ((QuotedItemResponse) i).setApproval(item.getApprovedItem().getApproval().getId());
                ((QuotedItemResponse) i).setApprovalItem(item.getApprovedItem().getId());
            }
            ((QuotedItemResponse) i).setProduct(productAdapter.transform(item.getProduct()));

        } else if (responseType == QuotedItemType.WITH_AVAILABLE_UNITS) {
            if (item.getApprovedItem() != null) {
                ((QuotedItemWithAvailableUnitsResponse) i).setApproval(item.getApprovedItem().getApproval().getId());
                ((QuotedItemWithAvailableUnitsResponse) i).setApprovalItem(item.getApprovedItem().getId());
            }
            ((QuotedItemWithAvailableUnitsResponse) i).setItem(productAdapter.transformToProductWithAvailableUnitsResponse(item.getProduct()));

        }

        if (item.getSuppliers() != null) {
            i.setSuppliers(
                    item.getSuppliers()
                            .stream()
                            .map(provided -> {
                                final SupplierItemQuotationResponse p = new SupplierItemQuotationResponse();
                                p.setSupplier(supplierAdapter.transform(provided.getSupplier()));
                                p.setQuantity(provided.getQuantity());
                                p.setUnit(unitAdapter.transform(provided.getUnit()));
                                p.setPrice(provided.getPrice());
                                p.setIpi(provided.getIpi());
                                p.setIcms(provided.getIcms());
                                p.setTotal(provided.getTotal());
                                p.setDiscount(provided.getDiscount());
                                p.setId(provided.getId());
                                p.setIsSelected(provided.getIsSelected());
                                return p;
                            })
                            .collect(Collectors.toList())
            );

            final SupplierItemQuotation selectedSupplier = item.getSuppliers()
                    .stream()
                    .filter(SupplierItemQuotation::getIsSelected)
                    .findFirst()
                    .orElse(new SupplierItemQuotation());

            i.setPrice(selectedSupplier.getPrice());
            i.setIpi(selectedSupplier.getIpi());
            i.setIcms(selectedSupplier.getIcms());
            i.setDiscount(selectedSupplier.getDiscount());
            i.setTotal(selectedSupplier.getTotal());
        }

        return i;
    }


    @Transactional
    public QuotedItemWithStageTraceResponse transform(QuotedItem item) {
        QuotedItemWithStageTraceResponse projection = new QuotedItemWithStageTraceResponse();

        projection.setId(item.getId());
        projection.setProduct(productAdapter.transform(item.getProduct()));
        projection.setQuantity(item.getQuantity());
        projection.setStatus(item.getStatus());
        projection.setUnit(unitAdapter.transform(item.getUnit()));
        projection.setLastStage(lastQuotedItemStageHandler(item));
        projection.setTotal(item.getSelectedSupplier() != null ? item.getSelectedSupplier().getTotal() : BigDecimal.ZERO);
        projection.setQuotation(transform(item.getQuotation()));

        return projection;
    }

    private Map<String, Object> lastQuotedItemStageHandler(QuotedItem item) {
        Map<String, Object> lastStage = new HashMap<>();

        if (item.getQuotation().getApproval() != null) {

            QuotationApproval approval = item.getQuotation().getApproval();
            if (approval.getOrders() != null && !approval.getOrders().isEmpty()) {

                PurchaseOrderItem orderItem = approval.getOrders().stream()
                        .filter(o -> o.getSupplier().equals(item.getSelectedSupplier().getSupplier()))
                        .map(o -> o.getItems().stream()
                                .filter(i -> i.getProduct().equals(item.getProduct()) &&
                                        i.getQuantity().equals(item.getQuantity()) &&
                                        i.getTotal().equals(item.getSelectedSupplier().getTotal()))
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

            lastStage.put("stage", "QUOTATION");
            lastStage.put("id", item.getQuotation().getId());
            lastStage.put("lastModifiedDate", item.getQuotation().getLastModifiedDate());
            lastStage.put("status", item.getQuotation().getStatus());

        }

        return lastStage;
    }

    public enum QuotedItemType {
        DEFAULT,
        WITH_TRACE,
        WITH_AVAILABLE_UNITS
    }

}
