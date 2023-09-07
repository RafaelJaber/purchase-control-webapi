package br.psi.giganet.api.purchase.quotations.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UpdateQuotedItemRequest {

    private Long id;

    private Long approvalItem;

    @NotEmpty(message = "Código do produto não pode ser nulo")
    private String code;

    @NotNull(message = "Quantidade do item de cotação não pode ser nulo")
    @Min(value = 0, message = "Quantidade deve ser maior ou igual a zero")
    private Double quantity;

    @NotNull(message = "Unidade do produto não pode ser nulo")
    private Long unit;

    @NotEmpty(message = "Deve ser informado pelo menos um fornecedor")
    @Valid
    private List<UpdateSupplierItemQuotedRequest> suppliers;

}
