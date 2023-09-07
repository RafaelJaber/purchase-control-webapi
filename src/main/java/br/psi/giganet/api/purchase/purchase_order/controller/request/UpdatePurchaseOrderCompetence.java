package br.psi.giganet.api.purchase.purchase_order.controller.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class UpdatePurchaseOrderCompetence {

    private Long id;

    @NotNull(message = "Data da competência não pode ser nula")
    private String date;

    @NotNull(message = "Centro de custo da competência não pode ser nulo")
    private Long costCenter;

    private String fiscalDocument;

    @NotNull(message = "Valor do faturamento não pode ser nulo")
    private BigDecimal total;
}
