package br.psi.giganet.api.purchase.suppliers.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupplierProjectionWithCPFAndCNPJ extends SupplierProjection {

    private String cpf;
    private String cnpj;

}
