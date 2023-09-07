package br.psi.giganet.api.purchase.payment_conditions.controller.response;

import lombok.Data;

@Data
public class PaymentConditionResponse {

    private Long id;
    private String name;
    private Integer numberOfInstallments;
    private Integer daysInterval;
    private String description;
}
