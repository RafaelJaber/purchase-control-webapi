package br.psi.giganet.api.purchase.approvals.repository;

import br.psi.giganet.api.purchase.approvals.model.Approval;
import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.purchase_requests.model.PurchaseRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface ApprovalRepository extends JpaRepository<Approval, Long> {

    List<Approval> findByRequest(PurchaseRequest request);

    @Query("SELECT a FROM Approval a WHERE a.status IN :status")
    List<Approval> findByStatus(List<ProcessStatus> status, Pageable pageable);

    @Transactional
    Long deleteByRequestAndStatus(PurchaseRequest request, ProcessStatus status);

    @Query("SELECT DISTINCT a FROM Approval a " +
            "INNER JOIN ApprovalItem i ON i.approval = a " +
            "LEFT JOIN QuotedItem qi ON qi.approvedItem = i " +
            "WHERE i.evaluation = 'APPROVED' AND " +
            "(qi IS NULL OR i.item.quantity > (" +
            " SELECT COALESCE(SUM(qit.quantity), 0) FROM QuotedItem qit WHERE qit.status IN ('APPROVED', 'REALIZED', 'PENDING' ) AND qit.approvedItem = i  )) " +
            "ORDER BY a.createdDate DESC")
    List<Approval> findAvailableToQuotation();
}
