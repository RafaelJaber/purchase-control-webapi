package br.psi.giganet.api.purchase.products.controller.response;

import lombok.Data;

@Data
public class ProductProjectionNameAndCodeOnly {

    private String code;
    private String name;

}
