package br.psi.giganet.api.purchase.purchase_order.controller.response;

import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.cost_center.controller.response.CostCenterResponse;
import br.psi.giganet.api.purchase.employees.controller.response.EmployeeProjection;
import br.psi.giganet.api.purchase.suppliers.controller.response.SupplierResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PurchaseOrderWebhookResponse {

    private Long id;
    private ProcessStatus status;
    private String description;
    private EmployeeProjection responsible;
    private CostCenterResponse costCenter;
    private String date;
    private String dateOfNeed;
    private List<PurchaseOrderItemWebhookResponse> items;
    private BigDecimal total;
    private String note;
    private OrderFreightResponse freight;
    private SupplierResponse supplier;

}
