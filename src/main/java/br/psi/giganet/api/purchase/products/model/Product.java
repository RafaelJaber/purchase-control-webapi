package br.psi.giganet.api.purchase.products.model;

import br.psi.giganet.api.purchase.config.security.model.AbstractModel;
import br.psi.giganet.api.purchase.products.categories.model.Category;
import br.psi.giganet.api.purchase.units.model.Unit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "products")
public class Product extends AbstractModel {

    @Column(nullable = false, unique = true)
    private String code;

    @NotEmpty
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_products_category"),
            name = "category",
            nullable = false,
            referencedColumnName = "id"
    )
    private Category category;

    @NotEmpty
    private String manufacturer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_products_unit"),
            name = "unit",
            nullable = false,
            referencedColumnName = "id"
    )
    private Unit unit;

    private String description;

}
