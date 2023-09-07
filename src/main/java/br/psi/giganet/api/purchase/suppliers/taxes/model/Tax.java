package br.psi.giganet.api.purchase.suppliers.taxes.model;

import br.psi.giganet.api.purchase.config.security.model.AbstractModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "taxes")
public class Tax extends AbstractModel {

    @Column(unique = true, nullable = false)
    private String stateFrom;

    @NotEmpty
    private String stateTo;

    @NotNull
    @PositiveOrZero
    private Float icms;

}
