package br.psi.giganet.api.purchase.approvals.controller.response;


import br.psi.giganet.api.purchase.branch_offices.controller.response.BranchOfficeProjection;
import br.psi.giganet.api.purchase.cost_center.controller.response.CostCenterResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ApprovalPurchaseReqResponse {
    private Long id;
    private String requester;
    private String responsible;
    private String reason;
    private CostCenterResponse costCenter;
    private BranchOfficeProjection branchOffice;
    private String note;
    private LocalDate dateOfNeed;
}
