package br.psi.giganet.api.purchase.suppliers.controller.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SupplierProjection {

    @EqualsAndHashCode.Include
    private Long id;
    private String name;

}
