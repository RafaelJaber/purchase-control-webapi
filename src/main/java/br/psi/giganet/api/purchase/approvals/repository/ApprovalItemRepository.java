package br.psi.giganet.api.purchase.approvals.repository;

import br.psi.giganet.api.purchase.approvals.model.ApprovalItem;
import br.psi.giganet.api.purchase.approvals.repository.projections.AvailableApprovalItem;
import br.psi.giganet.api.purchase.approvals.repository.projections.AvailableItemWithApprovalAndCostCenterAndBranchOffice;
import br.psi.giganet.api.purchase.quotations.model.Quotation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalItemRepository extends JpaRepository<ApprovalItem, Long> {

    @Query("SELECT i.id AS id, " +
            "pr AS item, " +
            "COALESCE(SUM(CASE WHEN (qi.status IN ('APPROVED', 'REALIZED', 'PENDING' )) THEN qi.quantity ELSE 0 END), 0) AS quotedQuantity " +
            "FROM ApprovalItem i " +
            "JOIN FETCH PurchaseRequestItem pr ON i.item = pr " +
            "LEFT JOIN QuotedItem qi ON qi.approvedItem = i " +
            "WHERE i.evaluation = 'APPROVED' AND " +
            "(i.isDiscarded IS NULL OR i.isDiscarded = FALSE) AND "+
            "i.approval.id = :approval AND " +
            "(qi IS NULL OR pr.quantity > (" +
            "SELECT COALESCE(SUM(qit.quantity), 0) FROM QuotedItem qit WHERE qit.status IN ('APPROVED', 'REALIZED', 'PENDING' ) AND qit.approvedItem = i  )) " +
            "GROUP BY i, pr")
    List<AvailableApprovalItem> findAvailableByApproval(Long approval);

    @Query("SELECT i.id AS id, " +
            "pr AS item, " +
            "COALESCE(SUM(CASE WHEN (qi.status IN ('APPROVED', 'REALIZED', 'PENDING' )) THEN qi.quantity ELSE 0 END), 0) AS quotedQuantity, " +
            "ap AS approval, " +
            "cc AS costCenter, " +
            "bo AS branchOffice " +
            "FROM ApprovalItem i " +
            "JOIN FETCH Approval ap ON i.approval = ap " +
            "JOIN FETCH PurchaseRequestItem pr ON i.item = pr " +
            "JOIN FETCH PurchaseRequest request ON pr.purchaseRequest = request " +
            "JOIN FETCH CostCenter cc ON request.costCenter = cc " +
            "JOIN FETCH BranchOffice bo ON request.branchOffice = bo " +
            "JOIN FETCH Product prod ON pr.product = prod " +
            "LEFT JOIN QuotedItem qi ON qi.approvedItem = i " +
            "WHERE i.evaluation = 'APPROVED' AND " +
            "(i.isDiscarded IS NULL OR i.isDiscarded = FALSE) AND "+
            "(qi IS NULL OR pr.quantity > (" +
            "SELECT COALESCE(SUM(qit.quantity), 0) FROM QuotedItem qit WHERE qit.status IN ('APPROVED', 'REALIZED', 'PENDING' ) AND qit.approvedItem = i  )) " +
            "GROUP BY i, pr, ap, cc, bo, prod " +
            "ORDER BY i.createdDate DESC")
    List<AvailableItemWithApprovalAndCostCenterAndBranchOffice> findAllAvailable(Pageable pageable);

    @Query("SELECT i.id AS id, " +
            "pr AS item, " +
            "COALESCE(SUM(" +
            "       CASE WHEN (qi.status IN ('APPROVED', 'REALIZED', 'PENDING' ) AND qo != :quotation ) " +
            "       THEN qi.quantity ELSE 0 END" +
            "   ), 0) AS quotedQuantity, " +
            "ap AS approval, " +
            "cc AS costCenter, " +
            "bo AS branchOffice " +
            "FROM ApprovalItem i " +
            "JOIN FETCH Approval ap ON i.approval = ap " +
            "JOIN FETCH PurchaseRequestItem pr ON i.item = pr " +
            "JOIN FETCH PurchaseRequest request ON pr.purchaseRequest = request " +
            "JOIN FETCH CostCenter cc ON request.costCenter = cc " +
            "JOIN FETCH BranchOffice bo ON request.branchOffice = bo " +
            "JOIN FETCH Product prod ON pr.product = prod " +
            "LEFT JOIN FETCH QuotedItem qi ON qi.approvedItem = i " +
            "LEFT JOIN FETCH Quotation qo ON qi.quotation = qo " +
            "WHERE i.evaluation = 'APPROVED' AND " +
            "(i.isDiscarded IS NULL OR i.isDiscarded = FALSE) AND "+
            "(qi IS NULL OR pr.quantity > (" +
            "   SELECT COALESCE(SUM(qit.quantity), 0) FROM QuotedItem qit " +
            "       INNER JOIN Quotation qqo ON qqo = qit.quotation " +
            "       WHERE qit.status IN ('APPROVED', 'REALIZED', 'PENDING' ) AND " +
            "             qit.approvedItem = i AND " +
            "             qqo != :quotation )) " +
            "GROUP BY i, pr, ap, cc, bo, prod " +
            "ORDER BY i.createdDate DESC ")
    List<AvailableItemWithApprovalAndCostCenterAndBranchOffice> findAllAvailableIgnoringQuotations(Quotation quotation, Pageable pageable);

}
