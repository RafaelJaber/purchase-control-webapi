package br.psi.giganet.api.purchase.quotations.controller.response;

import br.psi.giganet.api.purchase.suppliers.controller.response.SupplierProjectionAndEmail;
import lombok.Data;

import java.util.Set;

@Data
public class QuotationSuppliersResponse {

    private Long id;
    private Set<SupplierProjectionAndEmail> suppliers;

}
