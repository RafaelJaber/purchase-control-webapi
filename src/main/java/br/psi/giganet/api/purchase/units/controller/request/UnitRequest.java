package br.psi.giganet.api.purchase.units.controller.request;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class UnitRequest {

    @NotEmpty(message = "Nome é obrigatorio")
    private String name;
    private String description;
    @NotEmpty(message = "Abreviação é obrigatória")
    private String abbreviation;
    @Valid
    private List<UnitConversionRequest> conversions;

}
