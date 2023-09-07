package br.psi.giganet.api.purchase.purchase_order.controller.response;

import br.psi.giganet.api.purchase.branch_offices.controller.response.BranchOfficeProjection;
import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.employees.controller.response.EmployeeProjection;
import br.psi.giganet.api.purchase.suppliers.controller.response.SupplierProjection;
import lombok.Data;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
public class AdvancedOrderProjection {

    private Long id;
    private ZonedDateTime lastModifiedDate;
    private EmployeeProjection responsible;
    private Long approval;
    private Long quotation;
    private String description;
    private ProcessStatus status;
    private BigDecimal total;
    private SupplierProjection supplier;
    private BranchOfficeProjection branchOffice;

}
