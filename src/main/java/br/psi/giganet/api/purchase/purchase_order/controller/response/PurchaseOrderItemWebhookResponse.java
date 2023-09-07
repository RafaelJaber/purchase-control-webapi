package br.psi.giganet.api.purchase.purchase_order.controller.response;

import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.products.controller.response.ProductResponse;
import br.psi.giganet.api.purchase.units.controller.response.UnitResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
@Getter
@Setter
public class PurchaseOrderItemWebhookResponse {

    private Long id;
    private ProductResponse product;
    private Double quantity;
    private UnitResponse unit;
    private BigDecimal price;
    private Float ipi;
    private Float icms;
    private BigDecimal discount;
    private BigDecimal total;
    private ProcessStatus status;

}
