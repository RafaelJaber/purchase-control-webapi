package br.psi.giganet.api.purchase.approvals.model;

import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.config.security.model.AbstractModel;
import br.psi.giganet.api.purchase.employees.model.Employee;
import br.psi.giganet.api.purchase.purchase_requests.model.PurchaseRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "approvals")
public class Approval extends AbstractModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_approvals_request"),
            name = "request",
            nullable = false,
            referencedColumnName = "id")
    private PurchaseRequest request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_approvals_responsible"),
            name = "responsible",
            referencedColumnName = "id"
    )
    private Employee responsible;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ProcessStatus status;

    @OneToMany(
            mappedBy = "approval",
            fetch = FetchType.LAZY,
            cascade = {CascadeType.ALL},
            orphanRemoval = true
    )
    @NotEmpty
    private List<ApprovalItem> items;
    private String note;

}
