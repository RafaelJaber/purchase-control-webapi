package br.psi.giganet.api.purchase.purchase_order.model;

import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.config.security.model.AbstractModel;
import br.psi.giganet.api.purchase.products.model.Product;
import br.psi.giganet.api.purchase.suppliers.model.Supplier;
import br.psi.giganet.api.purchase.units.model.Unit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "purchase_order_items")
public class PurchaseOrderItem  extends AbstractModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_purchase_order_items_product"),
            name = "product",
            nullable = false,
            referencedColumnName = "id"
    )
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_purchase_order_items_purchase_order"),
            name = "purchase_order",
            nullable = false,
            referencedColumnName = "id")
    private PurchaseOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_purchase_order_items_supplier"),
            name = "supplier",
            nullable = false,
            referencedColumnName = "id"
    )
    private Supplier supplier;

    @NotNull
    @Min(0)
    private Double quantity;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_purchase_order_items_unit"),
            name = "unit",
            nullable = false,
            referencedColumnName = "id"
    )
    private Unit unit;

    @NotNull
    @Min(0)
    private BigDecimal price;

    @NotNull
    @Min(0)
    @Max(100)
    private Float ipi;

    @NotNull
    @Min(0)
    @Max(100)
    private Float icms;

    @Min(0)
    private BigDecimal discount;

    @NotNull
    @Min(0)
    private BigDecimal total;

    @Enumerated(value = EnumType.STRING)
    @NotNull
    private ProcessStatus status;
}
