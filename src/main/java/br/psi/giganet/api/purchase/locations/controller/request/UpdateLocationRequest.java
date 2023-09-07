package br.psi.giganet.api.purchase.locations.controller.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class UpdateLocationRequest {

    @NotNull(message = "Código ID é obrigatorio")
    private Long id;
    @NotEmpty(message = "Nome é obrigatorio")
    private String name;
    private String description;
    @NotNull(message = "Filial não pode ser nula")
    private Long branchOffice;

}
