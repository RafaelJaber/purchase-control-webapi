package br.psi.giganet.api.purchase.purchase_requests.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UpdatePurchaseRequestItem {

    private Long id;
    @NotNull(message = "Produto não pode ser nulo")
    private Long product;
    @NotNull(message = "Quantidade não pode ser vazia")
    @Min(value = 0, message = "Quantidade deve ser maior do que zero")
    private Double quantity;
    @NotNull(message = "Unidade não pode ser nulo")
    private Long unit;

}