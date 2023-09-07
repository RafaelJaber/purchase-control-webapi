package br.psi.giganet.api.purchase.suppliers.repository;

import br.psi.giganet.api.purchase.suppliers.model.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    List<Supplier> findByNameContainingIgnoreCase(String name);

    Page<Supplier> findByNameContainingIgnoreCase(String name, Pageable pageable);

}
