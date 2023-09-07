package br.psi.giganet.api.purchase.purchase_order.controller.response;

import br.psi.giganet.api.purchase.products.controller.response.ProductProjection;
import br.psi.giganet.api.purchase.suppliers.controller.response.SupplierProjection;
import br.psi.giganet.api.purchase.units.controller.response.UnitProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@NoArgsConstructor
@Getter
@Setter
public class LastPurchaseOrderItemResponse {

    private ZonedDateTime date;
    private ProductProjection product;
    private SupplierProjection supplier;
    private Double quantity;
    private UnitProjection unit;
    private BigDecimal price;
    private Float ipi;
    private Float icms;
    private BigDecimal discount;
    private BigDecimal total;

}
