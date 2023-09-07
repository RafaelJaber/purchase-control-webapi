package br.psi.giganet.api.purchase.purchase_requests.controller.response;

import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.units.controller.response.UnitProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequestItemResponse {
    private Long id;
    private PurchaseRequestItemProductResponse product;
    private Double quantity;
    private UnitProjection unit;
    private ProcessStatus status;
}