package br.psi.giganet.api.purchase.quotations.model;

import br.psi.giganet.api.purchase.approvals.model.ApprovalItem;
import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.common.utils.statuses.StatusesItem;
import br.psi.giganet.api.purchase.config.security.model.AbstractModel;
import br.psi.giganet.api.purchase.products.model.Product;
import br.psi.giganet.api.purchase.units.model.Unit;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "quoted_items")
public class QuotedItem extends AbstractModel implements StatusesItem {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_quoted_items_quotation"),
            name = "quotation",
            nullable = false,
            referencedColumnName = "id")
    private Quotation quotation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_quoted_items_approvedItem"),
            name = "approved_item",
            referencedColumnName = "id")
    private ApprovalItem approvedItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_quoted_items_product"),
            name = "product",
            nullable = false,
            referencedColumnName = "id"
    )
    private Product product;

    @NotNull
    private Double quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_quoted_items_unit"),
            name = "unit",
            nullable = false,
            referencedColumnName = "id"
    )
    private Unit unit;

    @OneToMany(
            mappedBy = "quotedItem",
            fetch = FetchType.LAZY,
            cascade = {CascadeType.ALL},
            orphanRemoval = true
    )
    private List<SupplierItemQuotation> suppliers;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ProcessStatus status;

    @JsonIgnore
    public SupplierItemQuotation getSelectedSupplier() {
        if (this.suppliers != null) {
            return this.suppliers.stream()
                    .filter(SupplierItemQuotation::getIsSelected)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

}
