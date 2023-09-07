package br.psi.giganet.api.purchase.units.model;

import br.psi.giganet.api.purchase.config.security.model.AbstractModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "units")
public class Unit extends AbstractModel {

    @NotEmpty
    private String name;
    private String abbreviation;
    private String description;

    @OneToMany(
            mappedBy = "from",
            fetch = FetchType.LAZY,
            cascade = {CascadeType.ALL}
    )
    private List<UnitConversion> conversions;

    public Boolean hasCompatibility(Unit unit) {
        return this.equals(unit) ||
                (this.conversions != null && this.conversions.stream().anyMatch(c -> c.hasConversion(unit)));
    }

}
