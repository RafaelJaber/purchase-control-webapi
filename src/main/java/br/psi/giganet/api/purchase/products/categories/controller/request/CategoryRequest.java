package br.psi.giganet.api.purchase.products.categories.controller.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class CategoryRequest {

    @NotEmpty(message = "Nome é obrigatorio")
    private String name;
    @NotEmpty(message = "Padrão é obrigatorio")
    private String pattern;
    private String description;

}
