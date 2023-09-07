package br.psi.giganet.api.purchase.approvals.controller.response;

import br.psi.giganet.api.purchase.branch_offices.controller.response.BranchOfficeProjection;
import br.psi.giganet.api.purchase.cost_center.controller.response.CostCenterResponse;
import br.psi.giganet.api.purchase.products.controller.response.ProductWithAvailableUnitsResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ApprovalItemAvailableToQuotationResponse extends AbstractApprovalItemResponse {

    private ApprovalProjection approval;
    private LocalDate dateOfNeed;
    private ProductWithAvailableUnitsResponse item;
    private CostCenterResponse costCenter;
    private BranchOfficeProjection branchOffice;

}