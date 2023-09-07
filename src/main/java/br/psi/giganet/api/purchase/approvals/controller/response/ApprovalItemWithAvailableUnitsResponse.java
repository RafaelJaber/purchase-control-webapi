package br.psi.giganet.api.purchase.approvals.controller.response;

import br.psi.giganet.api.purchase.products.controller.response.ProductWithAvailableUnitsResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ApprovalItemWithAvailableUnitsResponse extends AbstractApprovalItemResponse {

    private Long approval;
    private ProductWithAvailableUnitsResponse item;

}