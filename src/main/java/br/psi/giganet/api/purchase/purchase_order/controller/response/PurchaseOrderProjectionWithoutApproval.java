package br.psi.giganet.api.purchase.purchase_order.controller.response;

import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.employees.controller.response.EmployeeProjection;
import br.psi.giganet.api.purchase.suppliers.controller.response.SupplierProjection;
import lombok.Data;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
public class PurchaseOrderProjectionWithoutApproval {

    private Long id;
    private Long approval;
    private ProcessStatus status;
    private EmployeeProjection responsible;
    private ZonedDateTime createdDate;
    private ZonedDateTime lastModifiedDate;
    private BigDecimal total;
    private SupplierProjection supplier;

}
