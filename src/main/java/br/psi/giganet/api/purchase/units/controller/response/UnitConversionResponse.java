package br.psi.giganet.api.purchase.units.controller.response;

import lombok.Data;

@Data
public class UnitConversionResponse {

    private Long id;
    private UnitProjection to;
    private Double conversion;

}
