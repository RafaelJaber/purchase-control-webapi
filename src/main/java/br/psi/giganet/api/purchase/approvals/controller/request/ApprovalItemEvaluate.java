package br.psi.giganet.api.purchase.approvals.controller.request;


import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalItemEvaluate {
    @NotNull(message = "Id do item é necessário")
    private Long id;
    @NotNull(message = "Status da avaliação é necessário")
    private ProcessStatus status;
}