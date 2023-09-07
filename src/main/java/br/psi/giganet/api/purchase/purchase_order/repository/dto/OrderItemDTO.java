package br.psi.giganet.api.purchase.purchase_order.repository.dto;

import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public interface OrderItemDTO {

    String getProductCode();

    String getProductName();

    Long getPurchaseOrder();

    Long getSupplierId();

    String getSupplierName();

    ProcessStatus getStatus();

    ZonedDateTime getCreatedDate();

    BigDecimal getPrice();

}
