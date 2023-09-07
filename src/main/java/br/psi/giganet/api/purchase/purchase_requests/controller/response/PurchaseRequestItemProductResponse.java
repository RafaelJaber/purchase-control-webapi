package br.psi.giganet.api.purchase.purchase_requests.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequestItemProductResponse {
    private Long id;
    private String code;
    private String name;
}
