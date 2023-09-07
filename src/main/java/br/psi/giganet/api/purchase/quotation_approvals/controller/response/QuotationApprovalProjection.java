package br.psi.giganet.api.purchase.quotation_approvals.controller.response;

import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.employees.controller.response.EmployeeProjection;
import br.psi.giganet.api.purchase.quotations.controller.response.QuotationProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuotationApprovalProjection {

    private Long id;
    private ZonedDateTime date;
    private EmployeeProjection requester;
    private EmployeeProjection responsible;
    private QuotationProjection quotation;
    private ProcessStatus evaluation;

}
