package br.psi.giganet.api.purchase.quotations.repository;

import br.psi.giganet.api.purchase.quotations.model.QuotedItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface QuotedItemsRepository extends JpaRepository<QuotedItem, Long> {

    @Query("SELECT i FROM QuotedItem i WHERE UPPER(i.product.name) LIKE CONCAT('%', UPPER(:name), '%')")
    Page<QuotedItem> findByProductNameContainingIgnoreCase(String name, Pageable pageable);

}
