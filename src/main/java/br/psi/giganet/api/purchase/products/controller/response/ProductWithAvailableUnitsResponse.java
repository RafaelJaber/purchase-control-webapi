package br.psi.giganet.api.purchase.products.controller.response;

import br.psi.giganet.api.purchase.units.controller.response.UnitProjection;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class ProductWithAvailableUnitsResponse {

    private ProductProjection product;
    private Set<UnitProjection> availableUnits;

}
