package br.psi.giganet.api.purchase.quotations.repository;

import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.quotations.model.Quotation;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuotationRepository extends JpaRepository<Quotation, Long> {

    List<Quotation> findByStatus(ProcessStatus status);

    @Query("SELECT " +
            "q.id AS id, " +
            "q.createdDate AS createdDate, " +
            "q.description AS description, " +
            "q.total AS total, " +
            "q.responsible.id AS responsibleId, " +
            "q.responsible.name AS responsibleName, " +
            "q.branchOffice.id AS branchOfficeId, " +
            "q.branchOffice.name AS branchOfficeName, " +
            "q.branchOffice.shortName AS branchOfficeShortName, " +
            "q.status AS status " +
            "FROM Quotation q")
    List<QuotationEagerProjection> findAllWithFetchEager(Sort sort);

}
