package br.psi.giganet.api.purchase.quotations.controller.response;

import br.psi.giganet.api.purchase.products.controller.response.ProductWithAvailableUnitsResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class QuotedItemWithAvailableUnitsResponse extends AbstractQuotedItemResponse {

    private ProductWithAvailableUnitsResponse item;
    private Long approval;
    private Long approvalItem;

}
