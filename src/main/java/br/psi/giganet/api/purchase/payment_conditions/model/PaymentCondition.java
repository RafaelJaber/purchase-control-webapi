package br.psi.giganet.api.purchase.payment_conditions.model;

import br.psi.giganet.api.purchase.config.security.model.AbstractModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "payment_conditions")
public class PaymentCondition extends AbstractModel {

    @NotEmpty
    private String name;
    @Positive
    private Integer numberOfInstallments;
    @PositiveOrZero
    private Integer daysInterval;
    private String description;

}