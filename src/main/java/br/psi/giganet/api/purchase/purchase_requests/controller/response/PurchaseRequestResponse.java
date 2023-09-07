package br.psi.giganet.api.purchase.purchase_requests.controller.response;

import br.psi.giganet.api.purchase.branch_offices.controller.response.BranchOfficeProjection;
import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.cost_center.controller.response.CostCenterResponse;
import br.psi.giganet.api.purchase.employees.controller.response.EmployeeProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequestResponse {

    private Long id;
    private EmployeeProjection requester;
    private EmployeeProjection responsible;
    private String dateOfNeed;
    private ProcessStatus status;
    private String reason;
    private CostCenterResponse costCenter;
    private BranchOfficeProjection branchOffice;
    private String description;
    private String note;
    private List<PurchaseRequestItemResponse> products;

}
