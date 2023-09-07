package br.psi.giganet.api.purchase.purchase_order.controller.response;

import br.psi.giganet.api.purchase.branch_offices.controller.response.BranchOfficeProjection;
import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.cost_center.controller.response.CostCenterResponse;
import br.psi.giganet.api.purchase.employees.controller.response.EmployeeProjection;
import br.psi.giganet.api.purchase.locations.controller.response.LocationProjection;
import br.psi.giganet.api.purchase.projects.controller.response.ProjectResponse;
import br.psi.giganet.api.purchase.quotation_approvals.controller.response.QuotationApprovalProjection;
import br.psi.giganet.api.purchase.suppliers.controller.response.SupplierProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PurchaseOrderResponse {

    private Long id;
    private QuotationApprovalProjection approval;
    private ProcessStatus status;
    private EmployeeProjection responsible;
    private CostCenterResponse costCenter;
    private BranchOfficeProjection branchOffice;
    private ZonedDateTime createdDate;
    private ZonedDateTime lastModifiedDate;
    private LocalDate dateOfNeed;
    private List<PurchaseOrderItemResponse> items;
    private List<OrderPurchaseOrderCompetenceResponse> competencies;
    private BigDecimal total;
    private String note;
    private String externalLink;
    private OrderPaymentConditionResponse paymentCondition;
    private OrderFreightResponse freight;
    private SupplierProjection supplier;
    private ProjectResponse project;
    private LocationProjection location;

}
