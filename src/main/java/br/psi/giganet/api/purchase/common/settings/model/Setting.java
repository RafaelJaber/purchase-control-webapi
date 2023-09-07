package br.psi.giganet.api.purchase.common.settings.model;

import br.psi.giganet.api.purchase.config.security.model.AbstractModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "settings", uniqueConstraints = @UniqueConstraint(name = "uk_settings_key", columnNames = "key"))
public class Setting extends AbstractModel {

    @NotEmpty
    private String key;

    @NotNull
    private String value;

}
