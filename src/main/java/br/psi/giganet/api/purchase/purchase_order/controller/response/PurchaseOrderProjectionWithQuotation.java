package br.psi.giganet.api.purchase.purchase_order.controller.response;

import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.suppliers.controller.response.SupplierProjection;
import lombok.Data;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
public class PurchaseOrderProjectionWithQuotation {

    private Long id;
    private Long responsible;
    private Long approval;
    private Long quotation;
    private String description;
    private ProcessStatus status;
    private ZonedDateTime deliveryDate;
    private ZonedDateTime createdDate;
    private ZonedDateTime lastModifiedDate;
    private BigDecimal total;
    private SupplierProjection supplier;

}
