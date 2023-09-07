package br.psi.giganet.api.purchase.purchase_order.model;

import br.psi.giganet.api.purchase.config.security.model.AbstractModel;
import br.psi.giganet.api.purchase.cost_center.model.CostCenter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "purchase_order_competences")
public class PurchaseOrderCompetence extends AbstractModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_purchase_order_competences_purchase_order"),
            name = "purchase_order",
            nullable = false,
            referencedColumnName = "id")
    private PurchaseOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_purchase_order_competences_cost_center"),
            name = "costCenter",
            referencedColumnName = "id")
    private CostCenter costCenter;

    private String fiscalDocument;

    @NotNull
    private LocalDate date;

    @NotNull
    @Min(0)
    private BigDecimal total;

}
