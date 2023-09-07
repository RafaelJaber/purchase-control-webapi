package br.psi.giganet.api.purchase.purchase_order.controller.response;

import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.employees.controller.response.EmployeeProjection;
import br.psi.giganet.api.purchase.quotation_approvals.controller.response.QuotationApprovalProjection;
import br.psi.giganet.api.purchase.suppliers.controller.response.SupplierProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
public class PurchaseOrderProjection {

    private Long id;
    private QuotationApprovalProjection approval;
    private ProcessStatus status;
    private EmployeeProjection responsible;
    private ZonedDateTime createdDate;
    private ZonedDateTime lastModifiedDate;
    private BigDecimal total;
    private SupplierProjection supplier;

}
