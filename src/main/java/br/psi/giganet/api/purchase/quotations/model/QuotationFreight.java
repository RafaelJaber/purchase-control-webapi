package br.psi.giganet.api.purchase.quotations.model;

import br.psi.giganet.api.purchase.quotations.model.enums.FreightType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuotationFreight {

    @Column(name = "freight_type")
    @NotNull
    @Enumerated(EnumType.STRING)
    private FreightType type;

    @Column(name = "freight_price")
    @NotNull
    @Min(0)
    private BigDecimal price;

}
