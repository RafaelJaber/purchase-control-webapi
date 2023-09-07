package br.psi.giganet.api.purchase.quotations.model;

import br.psi.giganet.api.purchase.config.security.model.AbstractModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "quotation_payment_condition_due_dates")
public class QuotationConditionDueDate extends AbstractModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_quotation_payment_condition_due_dates_payment_condition"),
            name = "condition",
            nullable = false,
            referencedColumnName = "id")
    private QuotationPaymentCondition condition;

    @NotNull
    private LocalDate dueDate;

}
