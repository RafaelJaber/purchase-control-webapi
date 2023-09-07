package br.psi.giganet.api.purchase.delivery_addresses.model;

import br.psi.giganet.api.purchase.common.address.model.Address;
import br.psi.giganet.api.purchase.config.security.model.AbstractModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "delivery_addresses")
public class DeliveryAddress extends AbstractModel {

    @NotEmpty
    private String name;
    @Embedded
    private Address address;

}
