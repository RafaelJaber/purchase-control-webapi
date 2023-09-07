package br.psi.giganet.api.purchase.products.controller.response;

import br.psi.giganet.api.purchase.units.controller.response.UnitProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductProjection {

    private Long id;
    private String name;
    private String code;
    private UnitProjection unit;
    private String manufacturer;

}
