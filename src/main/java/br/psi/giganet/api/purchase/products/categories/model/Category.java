package br.psi.giganet.api.purchase.products.categories.model;

import br.psi.giganet.api.purchase.config.security.model.AbstractModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "product_categories")
public class Category extends AbstractModel {

    @NotEmpty
    private String name;
    @NotEmpty
    private String pattern;
    private String description;

}
