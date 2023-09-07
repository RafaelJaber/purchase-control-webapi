package br.psi.giganet.api.purchase.delivery_addresses.controller;

import br.psi.giganet.api.purchase.config.exception.exception.ResourceNotFoundException;
import br.psi.giganet.api.purchase.delivery_addresses.adapter.DeliveryAddressAdapter;
import br.psi.giganet.api.purchase.delivery_addresses.controller.request.DeliveryAddressRequest;
import br.psi.giganet.api.purchase.delivery_addresses.controller.response.DeliveryAddressResponse;
import br.psi.giganet.api.purchase.delivery_addresses.controller.security.RoleDeliverAddressWrite;
import br.psi.giganet.api.purchase.delivery_addresses.controller.security.RoleDeliveryAddressRead;
import br.psi.giganet.api.purchase.delivery_addresses.service.DeliveryAddressesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("delivery-addresses")
public class DeliveryAddressController {

    @Autowired
    private DeliveryAddressesService deliveryAddressesService;

    @Autowired
    private DeliveryAddressAdapter deliveryAddressAdapter;

    @GetMapping
    @RoleDeliveryAddressRead
    public List<DeliveryAddressResponse> findAll() {
        return deliveryAddressesService.findAll()
                .stream()
                .map(deliveryAddressAdapter::transform)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @RoleDeliveryAddressRead
    public DeliveryAddressResponse findById(@PathVariable Long id) {
        return deliveryAddressesService.findById(id)
                .map(deliveryAddressAdapter::transform)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço de entrega não encontrado"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RoleDeliverAddressWrite
    public DeliveryAddressResponse insert(@Valid @RequestBody DeliveryAddressRequest request) {
        return deliveryAddressesService.insert(deliveryAddressAdapter.transform(request))
                .map(deliveryAddressAdapter::transform)
                .orElseThrow(() -> new RuntimeException("Não foi possível cadastrar este endereço de entrega"));
    }

    @PutMapping("/{id}")
    @RoleDeliverAddressWrite
    public DeliveryAddressResponse update(
            @PathVariable Long id,
            @Valid @RequestBody DeliveryAddressRequest request) {
        return deliveryAddressesService.update(id, deliveryAddressAdapter.transform(request))
                .map(deliveryAddressAdapter::transform)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço de entrega não encontrado"));
    }


    @DeleteMapping("/{id}")
    @RoleDeliverAddressWrite
    public DeliveryAddressResponse deleteById(@PathVariable Long id) {
        return deliveryAddressesService.deleteById(id)
                .map(deliveryAddressAdapter::transform)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço de entrega não encontrado"));
    }
}
