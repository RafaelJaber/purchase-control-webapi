package br.psi.giganet.api.purchase.approvals.adapter;

import br.psi.giganet.api.purchase.approvals.controller.request.ApprovalEvaluateRequest;
import br.psi.giganet.api.purchase.approvals.controller.response.*;
import br.psi.giganet.api.purchase.approvals.model.Approval;
import br.psi.giganet.api.purchase.approvals.model.ApprovalItem;
import br.psi.giganet.api.purchase.approvals.repository.projections.AvailableApprovalItem;
import br.psi.giganet.api.purchase.approvals.repository.projections.AvailableItemWithApprovalAndCostCenterAndBranchOffice;
import br.psi.giganet.api.purchase.branch_offices.adapter.BranchOfficeAdapter;
import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.cost_center.adapter.CostCenterAdapter;
import br.psi.giganet.api.purchase.employees.adapter.EmployeeAdapter;
import br.psi.giganet.api.purchase.products.adapter.ProductAdapter;
import br.psi.giganet.api.purchase.purchase_requests.adapter.PurchaseRequestAdapter;
import br.psi.giganet.api.purchase.purchase_requests.model.PurchaseRequestItem;
import br.psi.giganet.api.purchase.units.adapter.UnitAdapter;
import br.psi.giganet.api.purchase.units.model.UnitConversion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.stream.Collectors;

@Component
public class ApprovalAdapter {

    @Autowired
    private PurchaseRequestAdapter purchaseRequestAdapter;
    @Autowired
    private ProductAdapter productAdapter;
    @Autowired
    private UnitAdapter unitAdapter;
    @Autowired
    private CostCenterAdapter costCenterAdapter;
    @Autowired
    private BranchOfficeAdapter branchOfficeAdapter;
    @Autowired
    private EmployeeAdapter employeeAdapter;

    public Approval create(final Long id) {
        final Approval approval = new Approval();
        approval.setId(id);

        return approval;
    }

    public ApprovalItem createItem(final Long id) {
        final ApprovalItem approval = new ApprovalItem();
        approval.setId(id);

        return approval;
    }

    public ApprovalItem createItem(final Long id, final Long approvalId) {
        final ApprovalItem approval = this.createItem(id);
        approval.setApproval(this.create(approvalId));

        return approval;
    }

    public Approval transform(ApprovalEvaluateRequest evaluate) {
        final Approval approval = new Approval();
        approval.setItems(
                evaluate.getItems()
                        .stream()
                        .map(e -> {
                            PurchaseRequestItem item = new PurchaseRequestItem();
                            item.setStatus(e.getStatus());

                            ApprovalItem approvalItem = new ApprovalItem();
                            approvalItem.setItem(item);
                            approvalItem.setId(e.getId());
                            approvalItem.setEvaluation(e.getStatus());

                            return approvalItem;
                        })
                        .collect(Collectors.toList())
        );
        approval.setNote(evaluate.getNote());

        return approval;
    }

    @Transactional
    public ApprovalProjection transform(Approval approval) {
        ApprovalProjection projection = new ApprovalProjection();
        projection.setId(approval.getId());
        projection.setRequest(approval.getRequest().getId());
        projection.setRequester(approval.getRequest().getRequester().getName());
        projection.setStatus(approval.getStatus());
        projection.setApprovalDate(approval.getLastModifiedDate());
        projection.setDescription(approval.getRequest().getDescription());

        return projection;
    }

    @Transactional
    public ApprovalResponse transformToFullResponse(Approval approval) {
        return transformToFullResponse(approval, ApprovalItemType.DEFAULT);
    }

    @Transactional
    public ApprovalResponse transformToFullResponse(Approval approval, ApprovalItemType responseType) {
        ApprovalResponse response = new ApprovalResponse();
        response.setId(approval.getId());

        response.setRequest(new ApprovalPurchaseReqResponse());
        response.getRequest().setId(approval.getRequest().getId());
        response.getRequest().setRequester(approval.getRequest().getRequester().getName());
        response.getRequest().setResponsible(approval.getRequest().getResponsible().getName());
        response.getRequest().setDateOfNeed(approval.getRequest().getDateOfNeed());
        response.getRequest().setReason(approval.getRequest().getReason());
        response.getRequest().setNote(approval.getRequest().getNote());
        response.getRequest().setCostCenter(costCenterAdapter.transform(approval.getRequest().getCostCenter()));
        response.getRequest().setBranchOffice(branchOfficeAdapter.transform(approval.getRequest().getBranchOffice()));

        response.setApprover(employeeAdapter.transform(approval.getResponsible()));

        response.setStatus(approval.getStatus());
        response.setNote(approval.getNote());
        response.setDescription(approval.getRequest().getDescription());


        response.setItems(
                approval.getItems()
                        .stream()
                        .map(i -> {
                            switch (responseType) {
                                case DEFAULT:
                                    return this.transform(i);
                                case WITH_APPROVAL:
                                    return this.transformWithApproval(i);
                                case WITH_AVAILABLE_UNITS:
                                    return this.transformWithAvailableUnits(i);
                                default:
                                    return null;
                            }
                        })
                        .collect(Collectors.toList()));

        return response;
    }

    public ApprovalItemResponse transform(ApprovalItem item) {
        final ApprovalItemResponse itemResponse = new ApprovalItemResponse();
        itemResponse.setId(item.getId());
        itemResponse.setProduct(productAdapter.transform(item.getItem().getProduct()));
        itemResponse.setQuantity(item.getItem().getQuantity());
        itemResponse.setStatus(item.getEvaluation());
        itemResponse.setUnit(unitAdapter.transform(item.getItem().getUnit()));
        return itemResponse;
    }

    @Transactional
    public AbstractApprovalItemResponse transformWithApproval(ApprovalItem item) {
        final ApprovalItemResponseWithApproval itemResponse = new ApprovalItemResponseWithApproval();
        itemResponse.setId(item.getId());
        itemResponse.setProduct(productAdapter.transform(item.getItem().getProduct()));
        itemResponse.setQuantity(item.getItem().getQuantity());
        itemResponse.setStatus(item.getEvaluation());
        itemResponse.setUnit(unitAdapter.transform(item.getItem().getUnit()));
        itemResponse.setApproval(item.getApproval().getId());
        return itemResponse;
    }

    @Transactional
    public AbstractApprovalItemResponse transformWithAvailableUnits(ApprovalItem item) {
        final ApprovalItemWithAvailableUnitsResponse itemResponse = new ApprovalItemWithAvailableUnitsResponse();
        itemResponse.setId(item.getId());
        itemResponse.setItem(productAdapter.transformToProductWithAvailableUnitsResponse(item.getItem().getProduct()));
        itemResponse.setQuantity(item.getItem().getQuantity());
        itemResponse.setStatus(item.getEvaluation());
        itemResponse.setUnit(unitAdapter.transform(item.getItem().getUnit()));
        itemResponse.setApproval(item.getApproval().getId());
        return itemResponse;
    }

    @Transactional
    public AbstractApprovalItemResponse transformWithAvailableUnits(AvailableApprovalItem item) {
        final ApprovalItemWithAvailableUnitsResponse itemResponse = new ApprovalItemWithAvailableUnitsResponse();
        itemResponse.setId(item.getId());
        itemResponse.setItem(productAdapter.transformToProductWithAvailableUnitsResponse(item.getItem().getProduct()));
        itemResponse.setQuantity(item.getRemainingQuantity());
        itemResponse.setUnit(unitAdapter.transform(item.getItem().getUnit()));
        return itemResponse;
    }

    @Transactional
    public AbstractApprovalItemResponse transformWithAvailableUnitsAndApprovalProjection(AvailableItemWithApprovalAndCostCenterAndBranchOffice item) {
        final ApprovalItemAvailableToQuotationResponse itemResponse = new ApprovalItemAvailableToQuotationResponse();
        itemResponse.setId(item.getId());
        itemResponse.setItem(productAdapter.transformToProductWithAvailableUnitsResponse(item.getItem().getProduct()));
        itemResponse.setQuantity(item.getRemainingQuantity());
        itemResponse.setUnit(unitAdapter.transform(item.getItem().getUnit()));
        itemResponse.setApproval(transform(item.getApproval()));
        itemResponse.setDateOfNeed(item.getApproval().getRequest().getDateOfNeed());
        itemResponse.setCostCenter(costCenterAdapter.transform(item.getCostCenter()));
        itemResponse.setBranchOffice(branchOfficeAdapter.transform(item.getBranchOffice()));
        return itemResponse;
    }

    @Transactional
    public ApprovalItemResponseWithTrace transformWithTrace(ApprovalItem item) {
        final ApprovalItemResponseWithTrace itemResponse = new ApprovalItemResponseWithTrace();
        itemResponse.setId(item.getId());
        itemResponse.setProduct(productAdapter.transform(item.getItem().getProduct()));
        itemResponse.setQuantity(item.getItem().getQuantity());
        itemResponse.setStatus(item.getEvaluation());
        itemResponse.setUnit(unitAdapter.transform(item.getItem().getUnit()));
        itemResponse.setApproval(item.getApproval().getId());

        if (item.getQuotedItems() != null) {
            itemResponse.setApprovedTrace(
                    new ItemTraceResponse(item.getItem().getUnit().getAbbreviation(), item.getQuotedItems().stream()
                            .filter(i -> i.getStatus().equals(ProcessStatus.APPROVED))
                            .map(i -> {
                                Double conversion = i.getUnit().getConversions().stream()
                                        .filter(unitConversion -> unitConversion.getTo().equals(item.getItem().getUnit()))
                                        .findFirst()
                                        .map(UnitConversion::getConversion)
                                        .orElse(1d);

                                return i.getQuantity() * conversion;
                            })
                            .reduce(Double::sum)
                            .orElse(0d)));

            itemResponse.setPendingTrace(
                    new ItemTraceResponse(item.getItem().getUnit().getAbbreviation(), item.getQuotedItems().stream()
                            .filter(i -> i.getStatus().equals(ProcessStatus.REALIZED))
                            .map(i -> {
                                Double conversion = i.getUnit().getConversions().stream()
                                        .filter(unitConversion -> unitConversion.getTo().equals(item.getItem().getUnit()))
                                        .findFirst()
                                        .map(UnitConversion::getConversion)
                                        .orElse(1d);

                                return i.getQuantity() * conversion;
                            })
                            .reduce(Double::sum)
                            .orElse(0d)));
        } else {
            itemResponse.setApprovedTrace(new ItemTraceResponse(null, -1d));
            itemResponse.setPendingTrace(new ItemTraceResponse(null, -1d));
        }

        return itemResponse;
    }

    public enum ApprovalItemType {
        DEFAULT,
        WITH_APPROVAL,
        WITH_AVAILABLE_UNITS
    }

}
