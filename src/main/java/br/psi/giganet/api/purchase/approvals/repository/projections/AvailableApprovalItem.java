package br.psi.giganet.api.purchase.approvals.repository.projections;

import br.psi.giganet.api.purchase.purchase_requests.model.PurchaseRequestItem;

import javax.transaction.Transactional;

public interface AvailableApprovalItem {

    Long getId();
    PurchaseRequestItem getItem();
    Double getQuotedQuantity();

    @Transactional
    default Double getRemainingQuantity() {
        return getItem().getQuantity() - getQuotedQuantity();
    }
}
