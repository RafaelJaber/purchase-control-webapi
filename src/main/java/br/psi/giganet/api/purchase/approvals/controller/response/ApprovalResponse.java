package br.psi.giganet.api.purchase.approvals.controller.response;

import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.employees.controller.response.EmployeeProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ApprovalResponse {

    private Long id;
    private EmployeeProjection approver;
    private ProcessStatus status;
    private LocalDateTime date;
    private String description;
    private String note;
    private ApprovalPurchaseReqResponse request;
    private List<AbstractApprovalItemResponse> items;

}
