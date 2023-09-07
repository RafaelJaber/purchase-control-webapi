package br.psi.giganet.api.purchase.payment_conditions.controller.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
public class PaymentConditionRequest {

    @NotEmpty(message = "Nome é obrigatorio")
    private String name;

    @NotNull(message = "Número de parcelas não pode ser nulo")
    @Positive(message = "Número de parcelas deve ser maior do que 0")
    private Integer numberOfInstallments;
    @Positive(message = "Intervalo de dias deve ser maior ou igual a que 0")
    private Integer daysInterval;
    private String description;

}
