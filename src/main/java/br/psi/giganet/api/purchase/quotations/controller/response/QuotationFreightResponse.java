package br.psi.giganet.api.purchase.quotations.controller.response;

import br.psi.giganet.api.purchase.quotations.model.enums.FreightType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class QuotationFreightResponse {

    private FreightType type;
    private BigDecimal price;

}
