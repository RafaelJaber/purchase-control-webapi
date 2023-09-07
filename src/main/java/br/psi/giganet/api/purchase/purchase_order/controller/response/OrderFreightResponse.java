package br.psi.giganet.api.purchase.purchase_order.controller.response;

import br.psi.giganet.api.purchase.common.address.model.Address;
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
public class OrderFreightResponse {

    private Long id;
    private FreightType type;
    private BigDecimal price;
    private String deliveryDate;
    private Address deliveryAddress;

}
