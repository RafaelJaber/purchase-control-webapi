package br.psi.giganet.api.purchase.purchase_order.repository.dto;

import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PurchaseOrderWithQuotationDTO {

    private Long id;
    private Long approval;
    private Long quotation;
    private String description;
    private ProcessStatus status;
    private ZonedDateTime deliveryDate;
    private ZonedDateTime createdDate;
    private ZonedDateTime lastModifiedDate;
    private BigDecimal total;
    private Long supplierId;
    private String supplierName;
    private Long branchOfficeId;
    private String branchOfficeName;
    private String branchOfficeShortName;
    private Long responsibleId;
    private String responsibleName;

}
