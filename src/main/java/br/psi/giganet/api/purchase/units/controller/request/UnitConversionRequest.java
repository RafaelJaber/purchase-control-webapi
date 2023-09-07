package br.psi.giganet.api.purchase.units.controller.request;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class UnitConversionRequest {

    private Long id;
    @NotNull(message = "Unidade de destino não pode ser nulo")
    private Long to;
    @NotNull(message = "Fator de conversão não pode ser nulo")
    @Min(value = 0, message = "Fator de conversão deve ser maior ou igual a zero")
    private Double conversion;

}
