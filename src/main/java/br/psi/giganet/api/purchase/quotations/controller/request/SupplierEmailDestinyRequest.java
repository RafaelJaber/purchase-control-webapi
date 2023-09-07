package br.psi.giganet.api.purchase.quotations.controller.request;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class SupplierEmailDestinyRequest {

    @NotNull(message = "Código do fornecedor não pode ser nulo")
    private Long id;

    @NotEmpty(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    private String email;

}
