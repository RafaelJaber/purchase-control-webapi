package br.psi.giganet.api.purchase.purchase_order.controller.response;

import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.products.controller.response.ProductProjectionNameAndCodeOnly;
import br.psi.giganet.api.purchase.suppliers.controller.response.SupplierProjection;
import lombok.Data;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
public class OrderItemProjection {

    private ProductProjectionNameAndCodeOnly product;
    private Long purchaseOrder;
    private SupplierProjection supplier;
    private ProcessStatus status;
    private ZonedDateTime createdDate;
    private BigDecimal price;

}
