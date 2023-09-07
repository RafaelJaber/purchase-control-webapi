package br.psi.giganet.api.purchase.delivery_addresses.adapter;

import br.psi.giganet.api.purchase.delivery_addresses.controller.request.DeliveryAddressRequest;
import br.psi.giganet.api.purchase.delivery_addresses.controller.response.DeliveryAddressResponse;
import br.psi.giganet.api.purchase.delivery_addresses.model.DeliveryAddress;
import org.springframework.stereotype.Component;

@Component
public class DeliveryAddressAdapter {

    public DeliveryAddress transform(DeliveryAddressRequest request) {
        DeliveryAddress deliveryAddress = new DeliveryAddress();
        deliveryAddress.setName(request.getName());
        deliveryAddress.setAddress(request.getAddress());
        return deliveryAddress;
    }

    public DeliveryAddressResponse transform(DeliveryAddress costCenter) {
        DeliveryAddressResponse response = new DeliveryAddressResponse();
        response.setId(costCenter.getId());
        response.setName(costCenter.getName());
        response.setAddress(costCenter.getAddress());

        return response;
    }

}
