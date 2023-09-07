package br.psi.giganet.api.purchase.purchase_requests.controller.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class InsertPurchaseRequestItem {

    @NotNull(message = "Produto não pode ser nulo")
    private Long product;
    @NotNull(message = "Quantidade não pode ser vazia")
    @Positive(message = "Quantidade deve ser maior do que zero")
    private Double quantity;
    @NotNull(message = "Unidade não pode ser nulo")
    private Long unit;

}