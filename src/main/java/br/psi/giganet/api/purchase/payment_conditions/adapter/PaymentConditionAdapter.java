package br.psi.giganet.api.purchase.payment_conditions.adapter;

import br.psi.giganet.api.purchase.payment_conditions.controller.request.PaymentConditionRequest;
import br.psi.giganet.api.purchase.payment_conditions.controller.response.PaymentConditionResponse;
import br.psi.giganet.api.purchase.payment_conditions.model.PaymentCondition;
import org.springframework.stereotype.Component;

@Component
public class PaymentConditionAdapter {

    public PaymentCondition create(Long id) {
        PaymentCondition paymentCondition = new PaymentCondition();
        paymentCondition.setId(id);

        return paymentCondition;
    }

    public PaymentCondition transform(PaymentConditionRequest request) {
        PaymentCondition paymentCondition = new PaymentCondition();
        paymentCondition.setName(request.getName());
        paymentCondition.setDaysInterval(request.getDaysInterval());
        paymentCondition.setNumberOfInstallments(request.getNumberOfInstallments());
        paymentCondition.setDescription(request.getDescription());
        return paymentCondition;
    }

    public PaymentConditionResponse transform(PaymentCondition paymentCondition) {
        PaymentConditionResponse response = new PaymentConditionResponse();
        response.setId(paymentCondition.getId());
        response.setName(paymentCondition.getName());
        response.setDaysInterval(paymentCondition.getDaysInterval());
        response.setNumberOfInstallments(paymentCondition.getNumberOfInstallments());
        response.setDescription(paymentCondition.getDescription());

        return response;
    }

}
