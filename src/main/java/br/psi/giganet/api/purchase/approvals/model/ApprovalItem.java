package br.psi.giganet.api.purchase.approvals.model;

import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.common.utils.statuses.StatusesItem;
import br.psi.giganet.api.purchase.config.security.model.AbstractModel;
import br.psi.giganet.api.purchase.purchase_requests.model.PurchaseRequestItem;
import br.psi.giganet.api.purchase.quotations.model.QuotedItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "approvals_items")
public class ApprovalItem extends AbstractModel implements StatusesItem {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_approvals_items_approval"),
            name = "approval",
            nullable = false,
            referencedColumnName = "id")
    private Approval approval;

    @OneToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_approvals_items_item"),
            name = "item",
            nullable = false,
            referencedColumnName = "id")
    private PurchaseRequestItem item;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "approvedItem")
    private List<QuotedItem> quotedItems;

    @Enumerated(value = EnumType.STRING)
    @NotNull
    private ProcessStatus evaluation;

    private Boolean isDiscarded;

    public Boolean isApproved() {
        return this.evaluation != null && this.evaluation.equals(ProcessStatus.APPROVED);
    }

    public Boolean isRejected() {
        return this.evaluation != null && this.evaluation.equals(ProcessStatus.REJECTED);
    }

    @Override
    public ProcessStatus getStatus() {
        return this.evaluation;
    }
}
