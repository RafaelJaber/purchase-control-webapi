package br.psi.giganet.api.purchase.quotations.model;

import br.psi.giganet.api.purchase.branch_offices.model.BranchOffice;
import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.config.security.model.AbstractModel;
import br.psi.giganet.api.purchase.cost_center.model.CostCenter;
import br.psi.giganet.api.purchase.employees.model.Employee;
import br.psi.giganet.api.purchase.locations.model.Location;
import br.psi.giganet.api.purchase.projects.model.Project;
import br.psi.giganet.api.purchase.quotation_approvals.model.QuotationApproval;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "quotations")
public class Quotation extends AbstractModel {

    private String note;
    @NotNull
    @Enumerated(EnumType.STRING)
    private ProcessStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_quotations_responsible"),
            name = "responsible",
            nullable = false,
            referencedColumnName = "id"
    )
    private Employee responsible;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_quotations_cost_center"),
            name = "costCenter",
            nullable = false,
            referencedColumnName = "id"
    )
    private CostCenter costCenter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_quotations_branch_office"),
            name = "branchOffice",
            nullable = false,
            referencedColumnName = "id"
    )
    private BranchOffice branchOffice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_quotations_project"),
            name = "project",
            referencedColumnName = "id"
    )
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_quotations_location"),
            name = "location",
            referencedColumnName = "id"
    )
    private Location location;

    private String description;

    @Column(length = 512)
    private String externalLink;

    private LocalDate dateOfNeed;

    @OneToMany(
            mappedBy = "quotation",
            fetch = FetchType.LAZY,
            cascade = {CascadeType.ALL},
            orphanRemoval = true
    )
    private List<QuotedItem> items;

    @NotNull
    @PositiveOrZero
    private BigDecimal total;

    @Embedded
    private QuotationFreight freight;

    @OneToOne(
            mappedBy = "quotation",
            cascade = {CascadeType.ALL},
            fetch = FetchType.LAZY,
            orphanRemoval = true)
    private QuotationPaymentCondition paymentCondition;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "quotation")
    private QuotationApproval approval;
}
