package br.psi.giganet.api.purchase.quotations.repository;

import br.psi.giganet.api.purchase.products.model.Product;
import br.psi.giganet.api.purchase.quotations.model.SupplierItemQuotation;
import br.psi.giganet.api.purchase.suppliers.model.Supplier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierItemQuotationRepository extends JpaRepository<SupplierItemQuotation, Long> {

    @Query("SELECT i FROM SupplierItemQuotation i WHERE " +
            "i.supplier = :supplier AND " +
            "i.quotedItem.product = :product")
    List<SupplierItemQuotation> findLastBySupplierAndProduct(Supplier supplier, Product product, Pageable pageable);

    @Query("SELECT i FROM SupplierItemQuotation i WHERE " +
            "i.supplier = :supplier AND " +
            "i.quotedItem.product.code = :product")
    List<SupplierItemQuotation> findLastBySupplierAndProduct(Supplier supplier, String product, Pageable pageable);

}
