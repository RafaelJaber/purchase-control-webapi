package br.psi.giganet.api.purchase.purchase_order.model;

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
@Table(name = "purchase_orders_payment_condition_due_dates")
public class OrderConditionDueDate extends AbstractModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_purchase_order_payment_condition_due_dates_payment_condition"),
            name = "condition",
            nullable = false,
            referencedColumnName = "id")
    private OrderPaymentCondition condition;

    @NotNull
    private LocalDate dueDate;

}
