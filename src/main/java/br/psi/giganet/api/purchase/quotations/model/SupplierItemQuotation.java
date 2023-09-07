package br.psi.giganet.api.purchase.quotations.model;

import br.psi.giganet.api.purchase.config.security.model.AbstractModel;
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
@Table(name = "supplier_item_quotation")
public class SupplierItemQuotation extends AbstractModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_supplier_item_quotation_quoted_item"),
            name = "quotedItem",
            nullable = false,
            referencedColumnName = "id"
    )
    private QuotedItem quotedItem;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_supplier_item_quotation_supplier"),
            name = "supplier",
            nullable = false,
            referencedColumnName = "id")
    private Supplier supplier;

    @NotNull
    @Min(0)
    private Double quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_supplier_item_quotation_unit"),
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
    private Float icms;

    @NotNull
    @Min(0)
    @Max(100)
    private Float ipi;

    @Min(0)
    private BigDecimal discount;

    @NotNull
    @Min(0)
    private BigDecimal total;

    @NotNull
    private Boolean isSelected;

}
