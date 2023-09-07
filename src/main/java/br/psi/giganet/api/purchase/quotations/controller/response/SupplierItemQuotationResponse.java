package br.psi.giganet.api.purchase.quotations.controller.response;


import br.psi.giganet.api.purchase.suppliers.controller.response.SupplierProjection;
import br.psi.giganet.api.purchase.units.controller.response.UnitProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SupplierItemQuotationResponse {

    private Long id;
    private SupplierProjection supplier;
    private Double quantity;
    private UnitProjection unit;
    private BigDecimal price;
    private Float ipi;
    private Float icms;
    private BigDecimal discount;
    private BigDecimal total;
    private Boolean isSelected;

}