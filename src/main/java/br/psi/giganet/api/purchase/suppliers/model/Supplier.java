package br.psi.giganet.api.purchase.suppliers.model;

import br.psi.giganet.api.purchase.common.address.model.Address;
import br.psi.giganet.api.purchase.config.security.model.AbstractModel;
import br.psi.giganet.api.purchase.suppliers.taxes.model.Tax;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.br.CNPJ;
import org.hibernate.validator.constraints.br.CPF;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "suppliers")
public class Supplier extends AbstractModel {

    @NotEmpty
    private String name;

    @Column(length = 14)
    @CNPJ
    private String cnpj;

    @Column(length = 11)
    @CPF
    private String cpf;

    @Column(length = 14)
    @Size(min = 2, max = 14)
    private String stateRegistration;

    @Column(length = 15)
    @Size(min = 1, max = 15)
    private String municipalRegistration;

    @NotEmpty
    @Email
    private String email;

    @Column(length = 15, nullable = false)
    private String cellphone;

    @Column(length = 15)
    private String telephone;

    private String description;

    @NotNull
    @Embedded
    private Address address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_suppliers_taxes"),
            name = "taxes",
            nullable = false,
            referencedColumnName = "id")
    private Tax tax;

}
