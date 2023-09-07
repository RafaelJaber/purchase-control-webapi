package br.psi.giganet.api.purchase.suppliers.taxes.controller.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

@Data
public class TaxRequest {

    @NotNull(message = "Código do registro não pode ser nulo")
    private Long id;
    @NotEmpty(message = "Estado de origem não pode ser nulo")
    private String from;
    @NotNull(message = "ICMS não pode ser nulo")
    @PositiveOrZero(message = "ICMS deve ser maior ou igual a zero")
    private Float icms;

}
