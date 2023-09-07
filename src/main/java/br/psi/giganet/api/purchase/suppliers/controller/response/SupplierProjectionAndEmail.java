package br.psi.giganet.api.purchase.suppliers.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupplierProjectionAndEmail extends SupplierProjection {

    private String email;

}
