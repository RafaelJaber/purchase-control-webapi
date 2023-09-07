package br.psi.giganet.api.purchase.quotation_approvals.controller.response;

import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.employees.controller.response.EmployeeProjection;
import br.psi.giganet.api.purchase.quotations.controller.response.QuotationResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuotationApprovalResponse {

    private Long id;
    private ZonedDateTime date;
    private EmployeeProjection responsible;
    private EmployeeProjection requester;
    private QuotationResponse quotation;
    private ProcessStatus evaluation;
    private String note;

}
