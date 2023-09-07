package br.psi.giganet.api.purchase.quotation_approvals.model;

import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.config.security.model.AbstractModel;
import br.psi.giganet.api.purchase.employees.model.Employee;
import br.psi.giganet.api.purchase.purchase_order.model.PurchaseOrder;
import br.psi.giganet.api.purchase.quotations.model.Quotation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "quotation_approvals")
public class QuotationApproval extends AbstractModel {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_quotation_approvals_quotation"),
            name = "quotation",
            nullable = false,
            unique = true,
            referencedColumnName = "id")
    private Quotation quotation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_quotation_approvals_responsible"),
            name = "responsible",
            referencedColumnName = "id"
    )
    private Employee responsible;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ProcessStatus evaluation;

    private String note;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "approval")
    private List<PurchaseOrder> orders;

    public boolean isApproved() {
        return this.evaluation != null && this.evaluation == ProcessStatus.APPROVED;
    }
}
