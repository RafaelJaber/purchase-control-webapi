package br.psi.giganet.api.purchase.purchase_order.controller.request;

import br.psi.giganet.api.purchase.common.address.model.Address;
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
public class PurchaseOrderFreightRequest {

    private Long id;
    @NotNull(message = "Tipo do frete é necessário")
    private FreightType type;
    private BigDecimal price;
    private String deliveryDate;
    private Address address;

}
