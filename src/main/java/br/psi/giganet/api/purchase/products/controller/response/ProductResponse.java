package br.psi.giganet.api.purchase.products.controller.response;

import br.psi.giganet.api.purchase.products.categories.controller.response.CategoryResponse;
import br.psi.giganet.api.purchase.units.controller.response.UnitResponse;
import lombok.Data;

@Data
public class ProductResponse {

    private Long id;
    private String code;
    private String name;
    private CategoryResponse category;
    private String manufacturer;
    private UnitResponse unit;
    private String description;

}
