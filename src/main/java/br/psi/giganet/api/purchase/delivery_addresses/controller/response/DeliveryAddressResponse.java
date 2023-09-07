package br.psi.giganet.api.purchase.delivery_addresses.controller.response;

import br.psi.giganet.api.purchase.common.address.model.Address;
import lombok.Data;

@Data
public class DeliveryAddressResponse {

    private Long id;
    private String name;
    private Address address;
}
