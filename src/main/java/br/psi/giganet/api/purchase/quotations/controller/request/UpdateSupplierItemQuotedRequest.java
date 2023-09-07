package br.psi.giganet.api.purchase.quotations.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UpdateSupplierItemQuotedRequest {

    private Long id;

    @NotNull(message = "Fornecedor deve possuir uma seleção")
    private Boolean isSelected;

    @NotNull(message = "Id do ifornecedor não pode ser nulo")
    private Long supplierId;

    @NotNull(message = "Preço do item da cotação não pode ser nulo")
    @Min(value = 0, message = "Preço do item da cotação deve ser maior ou igual a zero")
    private BigDecimal price;

    @NotNull(message = "Quantidade do item da cotação não pode ser nulo")
    @Min(value = 0, message = "Quantidade do item da cotação deve ser maior ou igual a zero")
    private Double quantity;

    @NotNull(message = "Unidade do produto não pode ser nulo")
    private Long unit;

    @NotNull(message = "IPI do item da cotação não pode ser nulo")
    @Min(value = 0, message = "IPI do item da cotação deve ser maior ou igual a zero")
    @Max(value = 100, message = "IPI do item da cotação deve ser maior ou igual a zero")
    private Float ipi;

    @NotNull(message = "ICMS do item da cotação não pode ser nulo")
    @Min(value = 0, message = "ICMS do item da cotação deve ser menor ou igual a 100")
    @Max(value = 100, message = "ICMS do item da cotação deve ser menor ou igual a 100")
    private Float icms;

    @Min(value = 0, message = "Desconto do item da cotação deve ser maior ou igual a zero")
    private BigDecimal discount;

    @NotNull(message = "Total do item de cotação não pode ser nulo")
    @Min(value = 0, message = "Total do item da cotação deve ser maior ou igual a zero")
    private BigDecimal total;

}
