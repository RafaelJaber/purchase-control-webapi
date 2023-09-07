package br.psi.giganet.api.purchase.units.controller.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UnitProjection {

    @EqualsAndHashCode.Include
    private Long id;
    private String name;
    private String abbreviation;

}
