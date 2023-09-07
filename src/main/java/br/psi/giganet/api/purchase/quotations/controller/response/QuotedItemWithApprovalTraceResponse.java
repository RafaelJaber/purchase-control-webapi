package br.psi.giganet.api.purchase.quotations.controller.response;

import br.psi.giganet.api.purchase.approvals.controller.response.ApprovalItemResponseWithTrace;
import br.psi.giganet.api.purchase.products.controller.response.ProductProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class QuotedItemWithApprovalTraceResponse extends AbstractQuotedItemResponse {

    private ProductProjection product;
    private ApprovalItemResponseWithTrace approvalItem;

}
