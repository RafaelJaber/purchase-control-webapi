package br.psi.giganet.api.purchase.quotations.repository;

import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public interface QuotationEagerProjection {

    Long getId();

    ZonedDateTime getCreatedDate();

    String getDescription();

    BigDecimal getTotal();

    ProcessStatus getStatus();

    Long getBranchOfficeId();

    String getBranchOfficeName();

    String getBranchOfficeShortName();

    Long getResponsibleId();

    String getResponsibleName();

}
