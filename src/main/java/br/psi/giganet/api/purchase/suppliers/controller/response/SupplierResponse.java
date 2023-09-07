package br.psi.giganet.api.purchase.suppliers.controller.response;

import br.psi.giganet.api.purchase.common.address.model.Address;
import br.psi.giganet.api.purchase.suppliers.taxes.controller.response.TaxResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierResponse {

    private Long id;
    private String name;
    private String cnpj;
    private String cpf;
    private String stateRegistration;
    private String municipalRegistration;
    private String email;
    private String cellphone;
    private String telephone;
    private String description;
    private Address address;
    private TaxResponse tax;
}
