package br.psi.giganet.api.purchase.branch_offices.controller.response;

import br.psi.giganet.api.purchase.common.address.model.Address;
import lombok.Data;

@Data
public class BranchOfficeResponse {

    private Long id;
    private String name;
    private String shortName;
    private String cnpj;
    private Address address;
    private String stateRegistration;
    private String telephone;

}
