package br.psi.giganet.api.purchase.cost_center.repository;

import br.psi.giganet.api.purchase.cost_center.model.CostCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CostCenterRepository extends JpaRepository<CostCenter, Long> {

}
