package br.psi.giganet.api.purchase.units.repository;

import br.psi.giganet.api.purchase.units.model.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnitRepository extends JpaRepository<Unit, Long> {

}
