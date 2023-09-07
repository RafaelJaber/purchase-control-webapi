package br.psi.giganet.api.purchase.quotation_approvals.repository;

import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.quotation_approvals.model.QuotationApproval;
import br.psi.giganet.api.purchase.quotations.model.Quotation;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuotationApprovalRepository extends JpaRepository<QuotationApproval, Long> {

    List<QuotationApproval> findByEvaluation(ProcessStatus evaluation);

    List<QuotationApproval> findByEvaluation(ProcessStatus evaluation, Sort sort);

    Optional<QuotationApproval> findByQuotation(Quotation quotation);

    @Query("SELECT " +
            "a.id AS id, " +
            "a.createdDate AS createdDate, " +
            "q.responsible AS requester, " +
            "a.responsible AS responsible, " +
            "q.id AS quotation, " +
            "q.branchOffice.id AS branchOfficeId, " +
            "q.branchOffice.name AS branchOfficeName, " +
            "q.branchOffice.shortName AS branchOfficeShortName, " +
            "q.total AS total, " +
            "a.evaluation AS evaluation " +
            "FROM QuotationApproval a " +
            "INNER JOIN a.quotation q " +
            "LEFT JOIN a.responsible" )
    List<ApprovalProjectionWithoutQuotation> findAllWithFetchEager(Sort sort);

}
