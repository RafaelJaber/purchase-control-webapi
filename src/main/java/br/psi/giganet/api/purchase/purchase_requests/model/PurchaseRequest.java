package br.psi.giganet.api.purchase.purchase_requests.model;

import br.psi.giganet.api.purchase.branch_offices.model.BranchOffice;
import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.config.security.model.AbstractModel;
import br.psi.giganet.api.purchase.cost_center.model.CostCenter;
import br.psi.giganet.api.purchase.employees.model.Employee;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "purchase_requests")
public class PurchaseRequest extends AbstractModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_purchase_request_employee"),
            name = "requester",
            nullable = false,
            referencedColumnName = "id"
    )
    private Employee requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_purchase_request_responsible"),
            name = "responsible",
            referencedColumnName = "id"
    )
    private Employee responsible;

    @NotEmpty
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_purchase_request_cost_center"),
            name = "costCenter",
            nullable = false,
            referencedColumnName = "id"
    )
    private CostCenter costCenter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_purchase_requests_branch_office"),
            name = "branchOffice",
//            nullable = false,
            referencedColumnName = "id"
    )
    private BranchOffice branchOffice;

    private LocalDate dateOfNeed;

    private String description;

    private String note;

    @Enumerated(value = EnumType.STRING)
    private ProcessStatus status;

    @OneToMany(
            mappedBy = "purchaseRequest",
            fetch = FetchType.LAZY,
            cascade = {CascadeType.ALL},
            orphanRemoval = true
    )
    @NotEmpty
    private List<PurchaseRequestItem> items;

}
