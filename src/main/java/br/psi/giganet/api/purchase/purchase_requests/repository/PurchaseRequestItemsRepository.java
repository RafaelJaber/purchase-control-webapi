package br.psi.giganet.api.purchase.purchase_requests.repository;

import br.psi.giganet.api.purchase.purchase_requests.model.PurchaseRequestItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseRequestItemsRepository  extends JpaRepository<PurchaseRequestItem, Long> {

    @Query("SELECT i FROM PurchaseRequestItem i WHERE UPPER(i.product.name) LIKE CONCAT('%', UPPER(:name), '%')")
    Page<PurchaseRequestItem> findByProductNameContainingIgnoreCase(String name, Pageable pageable);

}
