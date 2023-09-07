package br.psi.giganet.api.purchase.quotation_approvals.controller.request;

import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class InsertQuotationApprovalRequest {

    @NotNull(message = "Cotação é obrigatória")
    private Long quotation;
    private String note;
    @NotNull(message = "Avaliação é obrigatória")
    private ProcessStatus evaluation;

}
