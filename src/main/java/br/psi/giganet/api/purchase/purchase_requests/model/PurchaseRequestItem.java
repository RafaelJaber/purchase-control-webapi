package br.psi.giganet.api.purchase.purchase_requests.model;

import br.psi.giganet.api.purchase.approvals.model.ApprovalItem;
import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.common.utils.statuses.StatusesItem;
import br.psi.giganet.api.purchase.config.security.model.AbstractModel;
import br.psi.giganet.api.purchase.products.model.Product;
import br.psi.giganet.api.purchase.units.model.Unit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "purchase_request_items")
public class PurchaseRequestItem extends AbstractModel implements StatusesItem {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_purchase_request_items_product"),
            name = "product",
            nullable = false,
            referencedColumnName = "id"
    )
    private Product product;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_purchase_request_items_purchase_request"),
            name = "purchase_request",
            nullable = false,
            referencedColumnName = "id")
    private PurchaseRequest purchaseRequest;
    @NotNull
    @Min(0)
    private Double quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_purchase_request_items_unit"),
            name = "unit",
            nullable = false,
            referencedColumnName = "id"
    )
    private Unit unit;

    @Enumerated(value = EnumType.STRING)
    @NotNull
    private ProcessStatus status;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "item")
    private ApprovalItem approval;

    public Boolean isFinalized() {
        return ProcessStatus.APPROVED.equals(this.status) ||
                ProcessStatus.REJECTED.equals(this.status);
    }

}
