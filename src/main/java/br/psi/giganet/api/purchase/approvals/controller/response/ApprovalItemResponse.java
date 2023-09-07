package br.psi.giganet.api.purchase.approvals.controller.response;

import br.psi.giganet.api.purchase.products.controller.response.ProductProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ApprovalItemResponse extends AbstractApprovalItemResponse {

    private ProductProjection product;

}