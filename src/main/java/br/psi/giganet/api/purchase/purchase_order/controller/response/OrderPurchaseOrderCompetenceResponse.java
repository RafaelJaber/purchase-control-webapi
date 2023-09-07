package br.psi.giganet.api.purchase.purchase_order.controller.response;

import br.psi.giganet.api.purchase.cost_center.controller.response.CostCenterResponse;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class OrderPurchaseOrderCompetenceResponse {

    private Long id;
    private LocalDate date;
    private CostCenterResponse costCenter;
    private String fiscalDocument;
    private BigDecimal total;

}
