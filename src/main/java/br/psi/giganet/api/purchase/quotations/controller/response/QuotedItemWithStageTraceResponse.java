package br.psi.giganet.api.purchase.quotations.controller.response;

import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.products.controller.response.ProductProjection;
import br.psi.giganet.api.purchase.units.controller.response.UnitProjection;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class QuotedItemWithStageTraceResponse {

    private Long id;
    private ProductProjection product;
    private Double quantity;
    private UnitProjection unit;
    private ProcessStatus status;
    private BigDecimal total;
    private QuotationProjection quotation;

    private Map<String, Object> lastStage;

}
