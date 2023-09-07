package br.psi.giganet.api.purchase.common.address.controller;

import br.psi.giganet.api.purchase.common.address.model.Address;
import br.psi.giganet.api.purchase.common.address.service.AddressService;
import br.psi.giganet.api.purchase.config.exception.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/address")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping
    public Address findAddressByPostalCode(@RequestParam(name = "cep") String postalCode) {
        return addressService.findAddressByPostalCode(postalCode)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço não encontrado"));
    }

}
