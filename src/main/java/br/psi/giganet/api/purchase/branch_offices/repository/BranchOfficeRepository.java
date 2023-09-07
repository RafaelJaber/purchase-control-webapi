package br.psi.giganet.api.purchase.branch_offices.repository;

import br.psi.giganet.api.purchase.branch_offices.model.BranchOffice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchOfficeRepository extends JpaRepository<BranchOffice, Long> {
}
