package br.psi.giganet.api.purchase.suppliers.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.br.CNPJ;
import org.hibernate.validator.constraints.br.CPF;

import javax.validation.constraints.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SupplierRequest {

    @NotEmpty(message = "Email nao pode ser vazio/nulo")
    @Email(message = "Email inválido")
    private String email;

    @NotEmpty(message = "Nome nao pode ser vazio/nulo")
    private String name;

    @NotEmpty(message = "Celular nao pode ser vazio/nulo")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Celular inválido")
    private String cellphone;

    @Pattern(regexp = "^$|^[0-9]{10,11}|null$", message = "Telefone inválido")
    private String telephone;

    @CNPJ(message = "CNPJ informado é inválido")
    private String cnpj;

    @CPF(message = "CPF informado é inválido")
    private String cpf;

    @Size(min = 2, max = 14, message = "Inscrição estadual inválida")
    private String stateRegistration;

    @Size(min = 1, max = 15, message = "Inscrição municipal inválida")
    private String municipalRegistration;

    private String description;

    @NotNull(message = "Endereço não pode ser nulo")
    private SupplierAddress address;

}
