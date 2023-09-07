package br.psi.giganet.api.purchase.approvals.repository.projections;

import br.psi.giganet.api.purchase.approvals.model.Approval;
import br.psi.giganet.api.purchase.branch_offices.model.BranchOffice;
import br.psi.giganet.api.purchase.cost_center.model.CostCenter;

public interface AvailableItemWithApprovalAndCostCenterAndBranchOffice extends AvailableApprovalItem {

    Approval getApproval();
    CostCenter getCostCenter();
    BranchOffice getBranchOffice();

}
