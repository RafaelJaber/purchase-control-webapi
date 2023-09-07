package br.psi.giganet.api.purchase.suppliers.taxes.repository;

import br.psi.giganet.api.purchase.suppliers.taxes.model.Tax;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaxRepository extends JpaRepository<Tax, Long> {

    Optional<Tax> findByStateFrom(String state);

}
