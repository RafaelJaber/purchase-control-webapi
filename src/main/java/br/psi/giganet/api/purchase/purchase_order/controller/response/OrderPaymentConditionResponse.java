package br.psi.giganet.api.purchase.purchase_order.controller.response;

import br.psi.giganet.api.purchase.payment_conditions.controller.response.PaymentConditionResponse;
import lombok.Data;

import java.util.List;

@Data
public class OrderPaymentConditionResponse {

    private Long id;
    private PaymentConditionResponse condition;
    private List<OrderConditionDueDateResponse> dueDates;

}
