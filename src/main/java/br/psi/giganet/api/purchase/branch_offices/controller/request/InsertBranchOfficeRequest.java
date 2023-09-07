package br.psi.giganet.api.purchase.branch_offices.controller.request;

import lombok.Data;
import org.hibernate.validator.constraints.br.CNPJ;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class InsertBranchOfficeRequest {

    @NotEmpty(message = "Nome da filial não pode ser nulo")
    private String name;
    @NotEmpty(message = "Nome curto da filial não pode ser nulo")
    private String shortName;
    @NotEmpty(message = "CNPJ não pode ser nulo")
    @CNPJ(message = "CNPJ informado é inválido")
    private String cnpj;

    @Pattern(regexp = "^$|^[0-9]{10,11}|null$", message = "Telefone inválido")
    private String telephone;

    @Size(min = 2, max = 14, message = "Inscrição estadual inválida")
    private String stateRegistration;

    @NotNull(message = "Endereço não pode ser nulo")
    private BranchOfficeAddressRequest address;

}
