package br.psi.giganet.api.purchase.suppliers.taxes.controller.response;

import lombok.Data;

@Data
public class TaxResponse {

    private Long id;
    private String from;
    private String to;
    private Float icms;

}
