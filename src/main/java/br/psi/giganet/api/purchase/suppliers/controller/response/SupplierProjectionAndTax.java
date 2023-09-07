package br.psi.giganet.api.purchase.suppliers.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupplierProjectionAndTax extends SupplierProjection {

    private Float icms;

}
