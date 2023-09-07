package br.psi.giganet.api.purchase.delivery_addresses.controller.request;

import br.psi.giganet.api.purchase.common.address.model.Address;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class DeliveryAddressRequest {

    @NotEmpty(message = "Nome é obrigatorio")
    private String name;
    @NotNull(message = "Endereço não pode ser nulo")
    @Valid
    private Address address;

}
