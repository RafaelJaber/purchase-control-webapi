package br.psi.giganet.api.purchase.purchase_order.model;

import br.psi.giganet.api.purchase.branch_offices.model.BranchOffice;
import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.config.security.model.AbstractModel;
import br.psi.giganet.api.purchase.cost_center.model.CostCenter;
import br.psi.giganet.api.purchase.employees.model.Employee;
import br.psi.giganet.api.purchase.locations.model.Location;
import br.psi.giganet.api.purchase.projects.model.Project;
import br.psi.giganet.api.purchase.quotation_approvals.model.QuotationApproval;
import br.psi.giganet.api.purchase.suppliers.model.Supplier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder extends AbstractModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_purchase_orders_approval"),
            name = "approval",
            nullable = false,
            referencedColumnName = "id")
    private QuotationApproval approval;

    private String note;
    @NotNull
    @Enumerated(EnumType.STRING)
    private ProcessStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_purchase_orders_responsible"),
            name = "responsible",
            nullable = false,
            referencedColumnName = "id"
    )
    private Employee responsible;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_purchase_orders_cost_center"),
            name = "costCenter",
            nullable = false,
            referencedColumnName = "id"
    )
    private CostCenter costCenter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_purchase_orders_branch_office"),
            name = "branchOffice",
            nullable = false,
            referencedColumnName = "id"
    )
    private BranchOffice branchOffice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_purchase_orders_project"),
            name = "project",
            referencedColumnName = "id"
    )
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_purchase_orders_location"),
            name = "location",
            referencedColumnName = "id"
    )
    private Location location;

    private LocalDate dateOfNeed;

    @NotNull
    @Min(0)
    private BigDecimal total;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_purchase_orders_supplier"),
            name = "supplier",
            nullable = false,
            referencedColumnName = "id")
    private Supplier supplier;

    @NotEmpty
    @OneToMany(
            mappedBy = "order",
            fetch = FetchType.LAZY,
            cascade = {CascadeType.ALL},
            orphanRemoval = true
    )
    private List<PurchaseOrderItem> items;

    @NotNull
    @OneToOne(
            mappedBy = "order",
            fetch = FetchType.LAZY,
            cascade = {CascadeType.ALL},
            orphanRemoval = true
    )
    private OrderPaymentCondition paymentCondition;

    @OneToOne(
            mappedBy = "order",
            fetch = FetchType.LAZY,
            cascade = {CascadeType.ALL},
            orphanRemoval = true
    )
    private PurchaseOrderFreight freight;

    @OneToMany(
            mappedBy = "order",
            fetch = FetchType.LAZY,
            cascade = {CascadeType.ALL},
            orphanRemoval = true
    )
    private List<PurchaseOrderCompetence> competencies;

    private String fiscalDocument;

    @Column(length = 512)
    private String externalLink;
}
