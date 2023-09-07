package br.psi.giganet.api.purchase.purchase_order.repository;

import br.psi.giganet.api.purchase.purchase_order.repository.dto.AdvancedPurchaseOrderDTO;
import br.psi.giganet.api.purchase.purchase_order.repository.dto.OrderWithQuotationAndCompetenciesDTO;
import br.psi.giganet.api.purchase.purchase_order.repository.dto.PurchaseOrderWithQuotationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdvancedPurchaseOrderRepository {

    List<PurchaseOrderWithQuotationDTO> findAllWithQuotation(Sort sort);

    List<OrderWithQuotationAndCompetenciesDTO> findAllWithQuotationAndCompetencies();

    Page<AdvancedPurchaseOrderDTO> findAllByAdvancedSearch(List<String> queries, Pageable pageable);

}
