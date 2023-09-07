package br.psi.giganet.api.purchase.purchase_order.adapter;

import br.psi.giganet.api.purchase.branch_offices.adapter.BranchOfficeAdapter;
import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.cost_center.adapter.CostCenterAdapter;
import br.psi.giganet.api.purchase.employees.adapter.EmployeeAdapter;
import br.psi.giganet.api.purchase.locations.adapter.LocationAdapter;
import br.psi.giganet.api.purchase.payment_conditions.adapter.PaymentConditionAdapter;
import br.psi.giganet.api.purchase.products.adapter.ProductAdapter;
import br.psi.giganet.api.purchase.projects.adapter.ProjectAdapter;
import br.psi.giganet.api.purchase.purchase_order.controller.request.UpdatePurchaseOrderCompetenciesRequest;
import br.psi.giganet.api.purchase.purchase_order.controller.request.UpdatePurchaseOrderRequest;
import br.psi.giganet.api.purchase.purchase_order.controller.response.*;
import br.psi.giganet.api.purchase.purchase_order.model.*;
import br.psi.giganet.api.purchase.purchase_order.repository.dto.AdvancedPurchaseOrderDTO;
import br.psi.giganet.api.purchase.purchase_order.repository.dto.OrderItemDTO;
import br.psi.giganet.api.purchase.purchase_order.repository.dto.OrderWithQuotationAndCompetenciesDTO;
import br.psi.giganet.api.purchase.purchase_order.repository.dto.PurchaseOrderWithQuotationDTO;
import br.psi.giganet.api.purchase.quotation_approvals.adapter.QuotationApprovalAdapter;
import br.psi.giganet.api.purchase.suppliers.adapter.SupplierAdapter;
import br.psi.giganet.api.purchase.units.adapter.UnitAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PurchaseOrderAdapter {

    @Autowired
    private EmployeeAdapter employeeAdapter;
    @Autowired
    private QuotationApprovalAdapter quotationApprovalAdapter;
    @Autowired
    private ProductAdapter productAdapter;
    @Autowired
    private SupplierAdapter supplierAdapter;
    @Autowired
    private UnitAdapter unitAdapter;
    @Autowired
    private CostCenterAdapter costCenterAdapter;
    @Autowired
    private BranchOfficeAdapter branchOfficeAdapter;
    @Autowired
    private PaymentConditionAdapter paymentConditionAdapter;
    @Autowired
    private LocationAdapter locationAdapter;
    @Autowired
    private ProjectAdapter projectAdapter;

    public PurchaseOrder createOrder(Long id) {
        PurchaseOrder order = new PurchaseOrder();
        order.setId(id);
        return order;
    }

    public PurchaseOrderItem createOrderItem(Long id) {
        PurchaseOrderItem order = new PurchaseOrderItem();
        order.setId(id);
        return order;
    }

    public PurchaseOrder transform(final UpdatePurchaseOrderRequest request) {
        final PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setId(request.getId());
        purchaseOrder.setNote(request.getNote());
        purchaseOrder.setFreight(new PurchaseOrderFreight());
        purchaseOrder.getFreight().setId(request.getFreight().getId());
        purchaseOrder.getFreight().setDeliveryDate(request.getFreight().getDeliveryDate() != null ?
                ZonedDateTime.parse(request.getFreight().getDeliveryDate()) : null);
        purchaseOrder.getFreight().setDeliveryAddress(request.getFreight().getAddress());
        purchaseOrder.getFreight().setType(request.getFreight().getType());
        purchaseOrder.getFreight().setPrice(request.getFreight().getPrice());
        purchaseOrder.setStatus(request.getStatus());
        purchaseOrder.setBranchOffice(branchOfficeAdapter.create(request.getBranchOffice()));

        purchaseOrder.setPaymentCondition(new OrderPaymentCondition());
        purchaseOrder.getPaymentCondition().setId(request.getPaymentCondition().getId());
        purchaseOrder.getPaymentCondition().setOrder(purchaseOrder);
        purchaseOrder.getPaymentCondition().setCondition(
                paymentConditionAdapter.create(request.getPaymentCondition().getCondition()));
        purchaseOrder.getPaymentCondition().setDueDates(
                request.getPaymentCondition().getDueDates()
                        .stream()
                        .map(dueDate -> {
                            OrderConditionDueDate conditionDueDate = new OrderConditionDueDate();
                            conditionDueDate.setId(dueDate.getId());
                            conditionDueDate.setCondition(purchaseOrder.getPaymentCondition());
                            conditionDueDate.setDueDate(
                                    LocalDate.parse(dueDate.getDueDate()));

                            return conditionDueDate;
                        })
                        .collect(Collectors.toList())
        );

        purchaseOrder.setCompetencies(request.getCompetencies()
                .stream()
                .map(competence -> {
                    PurchaseOrderCompetence orderCompetence = new PurchaseOrderCompetence();
                    orderCompetence.setOrder(purchaseOrder);
                    orderCompetence.setCostCenter(costCenterAdapter.create(competence.getCostCenter()));
                    orderCompetence.setDate(LocalDate.parse(competence.getDate()));
                    orderCompetence.setFiscalDocument(competence.getFiscalDocument());
                    orderCompetence.setTotal(competence.getTotal());

                    return orderCompetence;
                })
                .collect(Collectors.toList()));

        return purchaseOrder;
    }

    public PurchaseOrder transform(final UpdatePurchaseOrderCompetenciesRequest request) {
        final PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setId(request.getId());
        purchaseOrder.setNote(request.getNote());
        purchaseOrder.setStatus(request.getStatus());

        purchaseOrder.setCompetencies(request.getCompetencies()
                .stream()
                .map(competence -> {
                    PurchaseOrderCompetence orderCompetence = new PurchaseOrderCompetence();
                    orderCompetence.setOrder(purchaseOrder);
                    orderCompetence.setCostCenter(costCenterAdapter.create(competence.getCostCenter()));
                    orderCompetence.setDate(LocalDate.parse(competence.getDate()));
                    orderCompetence.setFiscalDocument(competence.getFiscalDocument());
                    orderCompetence.setTotal(competence.getTotal());

                    return orderCompetence;
                })
                .collect(Collectors.toList()));

        return purchaseOrder;
    }

    public PurchaseOrderProjectionWithoutApproval transformToProjectionWithoutApproval(final PurchaseOrder order) {
        final PurchaseOrderProjectionWithoutApproval projection = new PurchaseOrderProjectionWithoutApproval();
        projection.setId(order.getId());
        projection.setStatus(order.getStatus());
        projection.setResponsible(employeeAdapter.transform(order.getResponsible()));
        projection.setSupplier(supplierAdapter.transform(order.getSupplier()));
        projection.setApproval(order.getApproval().getId());
        projection.setCreatedDate(order.getCreatedDate());
        projection.setLastModifiedDate(order.getLastModifiedDate());
        projection.setTotal(order.getTotal());

        return projection;
    }

    public PurchaseOrderProjectionWithQuotation transformToProjectionWithQuotation(
            final PurchaseOrderWithQuotationDTO order) {

        final PurchaseOrderProjectionWithQuotation projection = new PurchaseOrderProjectionWithQuotation();
        projection.setId(order.getId());
        projection.setResponsible(order.getResponsibleId());
        projection.setStatus(order.getStatus());
        projection.setSupplier(supplierAdapter.transform(order.getSupplierId(), order.getSupplierName()));
        projection.setApproval(order.getApproval());
        projection.setQuotation(order.getQuotation());
        projection.setDescription(order.getDescription());
        projection.setDeliveryDate(order.getDeliveryDate());
        projection.setCreatedDate(order.getCreatedDate());
        projection.setLastModifiedDate(order.getLastModifiedDate());
        projection.setTotal(order.getTotal());

        return projection;
    }

    public OrderProjectionWithQuotationAndCompetencies transformToProjectionWithQuotationAndCompetencies(
            final OrderWithQuotationAndCompetenciesDTO order) {

        final var projection = new OrderProjectionWithQuotationAndCompetencies();
        projection.setId(order.getId());
        projection.setResponsible(employeeAdapter.transform(order.getResponsibleId(), order.getResponsibleName()));
        projection.setStatus(order.getStatus());
        projection.setSupplier(supplierAdapter.transform(order.getSupplierId(), order.getSupplierName()));
        projection.setApproval(order.getApproval());
        projection.setQuotation(order.getQuotation());
        projection.setDescription(order.getDescription());
        projection.setDeliveryDate(order.getDeliveryDate());
        projection.setCompetencies(order.getCompetencies());
        projection.setTotal(order.getTotal());
        projection.setBranchOffice(branchOfficeAdapter.transform(
                order.getBranchOfficeId(),
                order.getBranchOfficeName(),
                order.getBranchOfficeShortName()));

        return projection;
    }

    public AdvancedOrderProjection transformToAdvancedOrderProjection(
            final AdvancedPurchaseOrderDTO order) {

        final var projection = new AdvancedOrderProjection();
        projection.setId(order.getId());
        projection.setLastModifiedDate(order.getLastModifiedDate());
        projection.setResponsible(employeeAdapter.transform(order.getResponsibleId(), order.getResponsibleName()));
        projection.setStatus(order.getStatus());
        projection.setSupplier(supplierAdapter.transform(order.getSupplierId(), order.getSupplierName()));
        projection.setApproval(order.getApproval());
        projection.setQuotation(order.getQuotation());
        projection.setDescription(order.getDescription());
        projection.setTotal(order.getTotal());
        projection.setBranchOffice(branchOfficeAdapter.transform(order.getBranchOffice()));

        return projection;
    }

    @Transactional
    public PurchaseOrderProjection transform(final PurchaseOrder order) {
        final PurchaseOrderProjection projection = new PurchaseOrderProjection();
        projection.setId(order.getId());
        projection.setStatus(order.getStatus());
        projection.setResponsible(employeeAdapter.transform(order.getResponsible()));
        projection.setSupplier(supplierAdapter.transform(order.getSupplier()));
        projection.setApproval(quotationApprovalAdapter.transform(order.getApproval()));
        projection.setCreatedDate(order.getCreatedDate());
        projection.setLastModifiedDate(order.getLastModifiedDate());
        projection.setTotal(order.getTotal());

        return projection;
    }

    @Transactional
    public PurchaseOrderResponse transformToFullResponse(final PurchaseOrder order) {
        return transformToFullResponse(order, false);
    }

    @Transactional
    public PurchaseOrderItemResponse transform(PurchaseOrderItem item) {
        final PurchaseOrderItemResponse i = new PurchaseOrderItemResponse();
        i.setId(item.getId());
        i.setPrice(item.getPrice());
        i.setProduct(productAdapter.transform(item.getProduct()));
        i.setQuantity(item.getQuantity());
        i.setUnit(unitAdapter.transform(item.getUnit()));
        i.setSupplier(supplierAdapter.transform(item.getSupplier()));
        i.setIcms(item.getIcms());
        i.setIpi(item.getIpi());
        i.setStatus(item.getStatus());
        i.setDiscount(item.getDiscount());
        i.setTotal(item.getTotal());
        return i;
    }

    @Transactional
    public PurchaseOrderItemResponse transformWithDetails(PurchaseOrderItem item) {
        final ItemResponseWithDetails i = new ItemResponseWithDetails();
        i.setId(item.getId());
        i.setPrice(item.getPrice());
        i.setProduct(productAdapter.transform(item.getProduct()));
        i.setQuantity(item.getQuantity());
        i.setUnit(unitAdapter.transform(item.getUnit()));
        i.setSupplier(supplierAdapter.transform(item.getSupplier()));
        i.setIcms(item.getIcms());
        i.setIpi(item.getIpi());
        i.setStatus(item.getStatus());
        i.setDiscount(item.getDiscount());
        i.setTotal(item.getTotal());
        i.setPurchaseOrder(item.getOrder().getId());
        i.setDate(item.getCreatedDate());
        return i;
    }

    @Transactional
    public OrderItemProjection transformToOrderItemProjection(OrderItemDTO item) {
        final OrderItemProjection i = new OrderItemProjection();
        i.setCreatedDate(item.getCreatedDate());
        i.setProduct(productAdapter.transformToNameAndCodeOnly(item.getProductCode(), item.getProductName()));
        i.setSupplier(supplierAdapter.transform(item.getSupplierId(), item.getSupplierName()));
        i.setStatus(item.getStatus());
        i.setPurchaseOrder(item.getPurchaseOrder());
        i.setPrice(item.getPrice());
        return i;
    }

    @Transactional
    public LastPurchaseOrderItemResponse transformToLastItemResponse(PurchaseOrderItem item) {
        final LastPurchaseOrderItemResponse i = new LastPurchaseOrderItemResponse();
        i.setDate(item.getCreatedDate());
        i.setPrice(item.getPrice());
        i.setProduct(productAdapter.transform(item.getProduct()));
        i.setQuantity(item.getQuantity());
        i.setUnit(unitAdapter.transform(item.getUnit()));
        i.setIcms(item.getIcms());
        i.setIpi(item.getIpi());
        i.setSupplier(supplierAdapter.transform(item.getSupplier()));
        i.setDiscount(item.getDiscount());
        i.setTotal(item.getTotal());
        return i;
    }

    @Transactional
    public PurchaseOrderResponse transformToFullResponse(final PurchaseOrder order, boolean filterPendingItems) {
        final PurchaseOrderResponse response = new PurchaseOrderResponse();
        response.setId(order.getId());
        response.setSupplier(supplierAdapter.transform(order.getSupplier()));
        response.setStatus(order.getStatus());
        response.setResponsible(employeeAdapter.transform(order.getResponsible()));
        response.setApproval(quotationApprovalAdapter.transform(order.getApproval()));
        response.setCostCenter(costCenterAdapter.transform(order.getCostCenter()));
        response.setBranchOffice(order.getBranchOffice() != null ? branchOfficeAdapter.transform(order.getBranchOffice()) : null);
        response.setDateOfNeed(order.getDateOfNeed());
        response.setCreatedDate(order.getCreatedDate());
        response.setLastModifiedDate(order.getLastModifiedDate());
        response.setTotal(order.getTotal());
        response.setNote(order.getNote());
        response.setExternalLink(order.getExternalLink());
        response.setLocation(order.getLocation() != null ? locationAdapter.transform(order.getLocation()) : null);
        response.setProject(order.getProject() != null ? projectAdapter.transform(order.getProject()) : null);
        response.setPaymentCondition(new OrderPaymentConditionResponse());
        response.getPaymentCondition().setId(order.getPaymentCondition().getId());
        response.getPaymentCondition()
                .setCondition(paymentConditionAdapter.transform(
                        order.getPaymentCondition().getCondition()));
        response.getPaymentCondition()
                .setDueDates(order.getPaymentCondition()
                        .getDueDates()
                        .stream()
                        .map(dueDate -> {
                            OrderConditionDueDateResponse dueDateResponse = new OrderConditionDueDateResponse();
                            dueDateResponse.setId(dueDate.getId());
                            dueDateResponse.setDueDate(dueDate.getDueDate());
                            return dueDateResponse;
                        })
                        .collect(Collectors.toList()));
        response.setItems(
                order.getItems()
                        .stream()
                        .filter(item -> !filterPendingItems || item.getStatus().equals(ProcessStatus.PENDING))
                        .map(this::transform)
                        .collect(Collectors.toList())
        );
        response.setFreight(new OrderFreightResponse());
        response.getFreight().setId(order.getFreight().getId());
        response.getFreight().setDeliveryAddress(order.getFreight().getDeliveryAddress());
        response.getFreight().setDeliveryDate(order.getFreight().getDeliveryDate() != null ?
                order.getFreight().getDeliveryDate().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null);
        response.getFreight().setPrice(order.getFreight().getPrice());
        response.getFreight().setType(order.getFreight().getType());

        if (order.getCompetencies() == null) {
            response.setCompetencies(Collections.emptyList());
        } else {
            response.setCompetencies(order.getCompetencies()
                    .stream()
                    .map(competence -> {
                        OrderPurchaseOrderCompetenceResponse competenceResponse = new OrderPurchaseOrderCompetenceResponse();
                        competenceResponse.setId(competence.getId());
                        competenceResponse.setDate(competence.getDate());
                        competenceResponse.setFiscalDocument(competence.getFiscalDocument());
                        competenceResponse.setCostCenter(competence.getCostCenter() == null ? null :
                                costCenterAdapter.transform(competence.getCostCenter()));
                        competenceResponse.setTotal(competence.getTotal());
                        return competenceResponse;
                    })
                    .collect(Collectors.toList()));
        }

        return response;
    }

    @Transactional
    public PurchaseOrderWebhookResponse transformToFullWebhookResponse(final PurchaseOrder order) {
        final PurchaseOrderWebhookResponse response = new PurchaseOrderWebhookResponse();
        response.setId(order.getId());
        response.setSupplier(supplierAdapter.transformToResponse(order.getSupplier()));
        response.setStatus(order.getStatus());
        response.setResponsible(employeeAdapter.transform(order.getResponsible()));
        response.setCostCenter(costCenterAdapter.transform(order.getCostCenter()));
        response.setDateOfNeed(order.getDateOfNeed() != null ? order.getDateOfNeed().toString() : null);
        response.setDate(order.getCreatedDate() != null ? order.getCreatedDate().toString() : null);
        response.setTotal(order.getTotal());
        response.setNote(order.getNote());
        response.setDescription(order.getApproval().getQuotation().getDescription());
        response.setItems(
                order.getItems()
                        .stream()
                        .map(this::transformToFullResponse)
                        .collect(Collectors.toList()));
        response.setFreight(new OrderFreightResponse());
        response.getFreight().setId(order.getFreight().getId());
        response.getFreight().setDeliveryAddress(order.getFreight().getDeliveryAddress());
        response.getFreight().setDeliveryDate(order.getFreight().getDeliveryDate() != null ?
                order.getFreight().getDeliveryDate().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null);
        response.getFreight().setPrice(order.getFreight().getPrice());
        response.getFreight().setType(order.getFreight().getType());

        return response;
    }


    @Transactional
    public PurchaseOrderItemWebhookResponse transformToFullResponse(PurchaseOrderItem item) {
        final PurchaseOrderItemWebhookResponse i = new PurchaseOrderItemWebhookResponse();
        i.setId(item.getId());
        i.setPrice(item.getPrice());
        i.setProduct(productAdapter.transformToFullResponse(item.getProduct()));
        i.setQuantity(item.getQuantity());
        i.setUnit(unitAdapter.transformToFullResponse(item.getUnit()));
        i.setIcms(item.getIcms());
        i.setIpi(item.getIpi());
        i.setStatus(item.getStatus());
        i.setDiscount(item.getDiscount());
        i.setTotal(item.getTotal());
        return i;
    }

    @SuppressWarnings("unchecked")
    public PurchaseOrder transform(Map<String, Object> request) {
        PurchaseOrder order = new PurchaseOrder();
        order.setId(Long.parseLong(request.get("id").toString()));
        order.setStatus(ProcessStatus.valueOf(request.get("status").toString()));
        order.setItems(
                request.containsKey("items") ?
                        ((List<HashMap<String, Object>>) request.get("items")).stream()
                                .map(item -> {
                                    PurchaseOrderItem orderItem = new PurchaseOrderItem();
                                    orderItem.setId(Long.parseLong(item.get("id").toString()));
                                    orderItem.setStatus(ProcessStatus.valueOf(item.get("status").toString()));

                                    return orderItem;
                                }).collect(Collectors.toList()) : Collections.emptyList());

        return order;
    }

}
