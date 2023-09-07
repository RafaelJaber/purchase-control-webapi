package br.psi.giganet.api.purchase.units.controller.response;

import lombok.Data;

import java.util.List;

@Data
public class UnitResponse {

    private Long id;
    private String name;
    private String description;
    private String abbreviation;
    private List<UnitConversionResponse> conversions;
}
