package br.psi.giganet.api.purchase.purchase_requests.controller.response;

import br.psi.giganet.api.purchase.branch_offices.controller.response.BranchOfficeProjection;
import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.employees.controller.response.EmployeeProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequestProjection {

    private Long id;
    private EmployeeProjection requester;
    private ZonedDateTime createdDate;
    private LocalDate dateOfNeed;
    private ProcessStatus status;
    private String description;
    private BranchOfficeProjection branchOffice;

}
