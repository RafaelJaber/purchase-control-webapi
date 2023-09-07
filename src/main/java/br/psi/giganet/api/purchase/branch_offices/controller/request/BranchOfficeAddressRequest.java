package br.psi.giganet.api.purchase.branch_offices.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BranchOfficeAddressRequest {

    @NotEmpty(message = "CEP não pode ser vazio/nulo")
    @Pattern(regexp = "^[0-9]{8}$", message = "CEP informado é inválido")
    private String postalCode;
    @NotEmpty(message = "Rua não pode ser vazia/nula")
    private String street;
    @NotEmpty(message = "Número não pode ser vazio/nulo")
    private String number;
    @NotEmpty(message = "Bairro não pode ser vazio/nulo")
    private String district;
    @NotEmpty(message = "Cidade não pode ser vazia/nula")
    private String city;
    @NotEmpty(message = "Estado não pode ser vazio/nulo")
    private String state;
    private String complement;

}
