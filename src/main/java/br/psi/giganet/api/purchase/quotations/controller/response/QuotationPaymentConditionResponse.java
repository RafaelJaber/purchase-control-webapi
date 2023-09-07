package br.psi.giganet.api.purchase.quotations.controller.response;

import br.psi.giganet.api.purchase.payment_conditions.controller.response.PaymentConditionResponse;
import lombok.Data;

import java.util.List;

@Data
public class QuotationPaymentConditionResponse {

    private Long id;
    private PaymentConditionResponse condition;
    private List<ConditionDueDateResponse> dueDates;

}
