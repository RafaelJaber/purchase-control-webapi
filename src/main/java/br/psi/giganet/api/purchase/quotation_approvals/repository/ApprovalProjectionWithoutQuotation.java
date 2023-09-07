package br.psi.giganet.api.purchase.quotation_approvals.repository;

import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.employees.model.Employee;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public interface ApprovalProjectionWithoutQuotation {

    Long getId();

    ZonedDateTime getCreatedDate();

    Employee getRequester();

    Employee getResponsible();

    Long getQuotation();

    Long getBranchOfficeId();

    String getBranchOfficeName();

    String getBranchOfficeShortName();

    BigDecimal getTotal();

    ProcessStatus getEvaluation();

}
