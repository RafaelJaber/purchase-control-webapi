package br.psi.giganet.api.purchase.quotations.controller.request;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class InsertPaymentConditionRequest {

    @NotNull(message = "Condição de pagamento não pode ser nulo")
    private Long condition;

    @NotEmpty(message = "Datas de vencimento não podem ser vazias")
    @Valid
    private List<InsertConditionDateDueRequest> dueDates;
}
