package br.psi.giganet.api.purchase.delivery_addresses.repository;

import br.psi.giganet.api.purchase.delivery_addresses.model.DeliveryAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddress, Long> {

}
