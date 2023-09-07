package br.psi.giganet.api.purchase.quotations.controller.response;

import br.psi.giganet.api.purchase.units.controller.response.UnitProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public abstract class AbstractQuotedItemResponse {
    private Long id;
    private Double quantity;
    private UnitProjection unit;
    private Float ipi;
    private Float icms;
    private BigDecimal price;
    private BigDecimal discount;
    private BigDecimal total;
    private List<SupplierItemQuotationResponse> suppliers;

}
