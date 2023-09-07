package br.psi.giganet.api.purchase.quotations.controller.response;

import br.psi.giganet.api.purchase.branch_offices.controller.response.BranchOfficeProjection;
import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.cost_center.controller.response.CostCenterResponse;
import br.psi.giganet.api.purchase.employees.controller.response.EmployeeProjection;
import br.psi.giganet.api.purchase.locations.controller.response.LocationProjection;
import br.psi.giganet.api.purchase.projects.controller.response.ProjectResponse;
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
public class QuotationResponse {

    private Long id;
    private ZonedDateTime date;
    private LocalDate dateOfNeed;
    private String description;
    private String externalLink;
    private String note;
    private EmployeeProjection responsible;
    private CostCenterResponse costCenter;
    private BranchOfficeProjection branchOffice;
    private ProcessStatus status;
    private List<AbstractQuotedItemResponse> items;
    private QuotationFreightResponse freight;
    private QuotationPaymentConditionResponse paymentCondition;
    private LocationProjection location;
    private ProjectResponse project;
    private BigDecimal total;

}
