package br.psi.giganet.api.purchase.products.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProductRequest {

    @NotEmpty(message = "Código nao pode ser vazio/nulo")
    private String code;
    @NotEmpty(message = "Nome nao pode ser vazio/nulo")
    private String name;
    @NotNull(message = "Categoria nao pode ser vazio/nulo")
    private Long category;
    @NotEmpty(message = "Fabricante nao pode ser vazio/nulo")
    private String manufacturer;
    @NotNull(message = "Unidade nao pode ser nulo")
    private Long unit;

    private String description;

}
