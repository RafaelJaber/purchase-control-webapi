package br.psi.giganet.api.purchase.quotations.model;

import br.psi.giganet.api.purchase.config.security.model.AbstractModel;
import br.psi.giganet.api.purchase.payment_conditions.model.PaymentCondition;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "quotation_payment_conditions")
public class QuotationPaymentCondition extends AbstractModel {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_quotation_payment_conditions_quotation"),
            name = "quotation",
            nullable = false,
            referencedColumnName = "id")
    private Quotation quotation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_quotation_payment_conditions_payment_condition"),
            name = "condition",
            nullable = false,
            referencedColumnName = "id")
    private PaymentCondition condition;

    @NotEmpty
    @OneToMany(
            mappedBy = "condition",
            cascade = {CascadeType.ALL},
            fetch = FetchType.LAZY,
            orphanRemoval = true)
    private List<QuotationConditionDueDate> dueDates;

}
