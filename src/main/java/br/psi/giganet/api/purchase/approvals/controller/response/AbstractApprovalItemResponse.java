package br.psi.giganet.api.purchase.approvals.controller.response;

import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.units.controller.response.UnitProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public abstract class AbstractApprovalItemResponse {
    private Long id;
    private Double quantity;
    private UnitProjection unit;
    private ProcessStatus status;

}