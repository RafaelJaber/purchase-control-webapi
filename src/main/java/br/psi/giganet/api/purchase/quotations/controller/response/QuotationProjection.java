package br.psi.giganet.api.purchase.quotations.controller.response;

import br.psi.giganet.api.purchase.branch_offices.controller.response.BranchOfficeProjection;
import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.employees.controller.response.EmployeeProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
public class QuotationProjection {

    private Long id;
    private ZonedDateTime date;
    private String description;
    private String note;
    private BigDecimal total;
    private ProcessStatus status;
    private BranchOfficeProjection branchOffice;
    private EmployeeProjection responsible;
}
