package br.psi.giganet.api.purchase.quotations.controller.request;

import br.psi.giganet.api.purchase.quotations.model.enums.FreightType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class QuotationFreightRequest {

    @NotNull(message = "Tipo do frete é necessário")
    private FreightType type;
    private BigDecimal price;

}
