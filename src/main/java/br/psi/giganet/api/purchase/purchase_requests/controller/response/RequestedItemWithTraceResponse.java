package br.psi.giganet.api.purchase.purchase_requests.controller.response;

import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.products.controller.response.ProductProjection;
import br.psi.giganet.api.purchase.units.controller.response.UnitProjection;
import lombok.Data;

import java.util.Map;

@Data
public class RequestedItemWithTraceResponse {

    private Long id;
    private ProductProjection product;
    private Double quantity;
    private UnitProjection unit;
    private ProcessStatus status;
    private PurchaseRequestProjection purchaseRequest;

    private Map<String, Object> lastStage;

}
