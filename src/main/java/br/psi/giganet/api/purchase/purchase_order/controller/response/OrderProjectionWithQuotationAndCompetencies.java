package br.psi.giganet.api.purchase.purchase_order.controller.response;

import br.psi.giganet.api.purchase.branch_offices.controller.response.BranchOfficeProjection;
import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.employees.controller.response.EmployeeProjection;
import br.psi.giganet.api.purchase.suppliers.controller.response.SupplierProjection;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@Data
public class OrderProjectionWithQuotationAndCompetencies {

    private Long id;
    private EmployeeProjection responsible;
    private Long approval;
    private Long quotation;
    private String description;
    private ProcessStatus status;
    private ZonedDateTime deliveryDate;
    private List<LocalDate> competencies;
    private BigDecimal total;
    private SupplierProjection supplier;
    private BranchOfficeProjection branchOffice;

}
