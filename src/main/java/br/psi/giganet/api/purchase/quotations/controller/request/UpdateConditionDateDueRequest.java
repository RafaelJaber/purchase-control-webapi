package br.psi.giganet.api.purchase.quotations.controller.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UpdateConditionDateDueRequest {

    private Long id;
    @NotNull(message = "Data de vencimento não pode ser nula")
    private String dueDate;
}
