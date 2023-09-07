package br.psi.giganet.api.purchase.branch_offices.model;

import br.psi.giganet.api.purchase.common.address.model.Address;
import br.psi.giganet.api.purchase.config.security.model.AbstractModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.br.CNPJ;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "branch_offices")
public class BranchOffice extends AbstractModel {

    @NotEmpty
    private String name;

    @NotEmpty
    private String shortName;

    @NotNull
    @Embedded
    private Address address;

    @Column(nullable = false, length = 14)
    @CNPJ
    private String cnpj;

    @Column(length = 14)
    @Size(min = 2, max = 14)
    private String stateRegistration;

    @Column(length = 15)
    private String telephone;

}
