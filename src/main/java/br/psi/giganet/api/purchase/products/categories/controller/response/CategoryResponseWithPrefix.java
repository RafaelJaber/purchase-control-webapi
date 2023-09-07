package br.psi.giganet.api.purchase.products.categories.controller.response;

import lombok.Data;

@Data
public class CategoryResponseWithPrefix extends CategoryResponse {
    private String pattern;
}
