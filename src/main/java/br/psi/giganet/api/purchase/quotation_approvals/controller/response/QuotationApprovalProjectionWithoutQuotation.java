package br.psi.giganet.api.purchase.quotation_approvals.controller.response;

import br.psi.giganet.api.purchase.branch_offices.controller.response.BranchOfficeProjection;
import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.employees.controller.response.EmployeeProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuotationApprovalProjectionWithoutQuotation {

    private Long id;
    private ZonedDateTime date;
    private EmployeeProjection requester;
    private EmployeeProjection responsible;
    private Long quotation;
    private BigDecimal total;
    private ProcessStatus evaluation;
    private BranchOfficeProjection branchOffice;

}
