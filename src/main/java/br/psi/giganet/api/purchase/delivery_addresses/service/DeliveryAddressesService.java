package br.psi.giganet.api.purchase.delivery_addresses.service;

import br.psi.giganet.api.purchase.common.address.model.Address;
import br.psi.giganet.api.purchase.delivery_addresses.model.DeliveryAddress;
import br.psi.giganet.api.purchase.delivery_addresses.repository.DeliveryAddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DeliveryAddressesService {

    @Autowired
    private DeliveryAddressRepository deliveryAddressRepository;

    public List<DeliveryAddress> findAll() {
        return deliveryAddressRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public DeliveryAddress getDeliveryAddressDefault() {
        return this.deliveryAddressRepository.findAll(PageRequest.of(0, 1))
                .getContent()
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    DeliveryAddress deliveryAddress = new DeliveryAddress();
                    Address address = new Address();
                    address.setPostalCode("35160296");
                    address.setComplement("");
                    address.setCity("Ipatinga");
                    address.setDistrict("Horto");
                    address.setState("MG");
                    address.setStreet("Rua Cedro");
                    address.setNumber("393");

                    deliveryAddress.setAddress(address);

                    return deliveryAddress;
                });
    }

    public Optional<DeliveryAddress> insert(DeliveryAddress address) {
        return Optional.of(deliveryAddressRepository.save(address));
    }

    public Optional<DeliveryAddress> findById(Long id) {
        return deliveryAddressRepository.findById(id);
    }

    public Optional<DeliveryAddress> update(Long id, DeliveryAddress address) {
        return deliveryAddressRepository.findById(id)
                .map(saved -> {
                    saved.setName(address.getName());
                    saved.setAddress(address.getAddress());

                    return deliveryAddressRepository.save(saved);
                });
    }

    public Optional<DeliveryAddress> deleteById(Long id) {
        return deliveryAddressRepository.findById(id)
                .map(address -> {
                    deliveryAddressRepository.delete(address);
                    return address;
                });
    }

}
