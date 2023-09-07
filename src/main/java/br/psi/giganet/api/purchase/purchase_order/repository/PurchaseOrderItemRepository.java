package br.psi.giganet.api.purchase.purchase_order.repository;

import br.psi.giganet.api.purchase.products.model.Product;
import br.psi.giganet.api.purchase.purchase_order.model.PurchaseOrderItem;
import br.psi.giganet.api.purchase.purchase_order.repository.dto.OrderItemDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, Long> {

    @Query("SELECT i FROM PurchaseOrderItem i WHERE " +
            "i.product.code = :product AND " +
            "i.status != 'CANCELED'")
    List<PurchaseOrderItem> findLastByProduct(String product, Pageable pageable);

    @Query("SELECT i FROM PurchaseOrderItem i WHERE " +
            "i.product = :product AND " +
            "i.status != 'CANCELED'")
    List<PurchaseOrderItem> findLastByProduct(Product product, Pageable pageable);

    @Query("SELECT " +
            "   i.product.code AS productCode, " +
            "   i.product.name AS productName, " +
            "   i.supplier.id AS supplierId, " +
            "   i.supplier.name AS supplierName, " +
            "   i.order.id AS purchaseOrder, " +
            "   i.status AS status, " +
            "   i.createdDate AS createdDate, " +
            "   i.price AS price " +
            "FROM PurchaseOrderItem i " +
            "WHERE i.status != 'CANCELED'")
    Page<OrderItemDTO> findAllByStatusNotCanceled(Pageable pageable);
}
