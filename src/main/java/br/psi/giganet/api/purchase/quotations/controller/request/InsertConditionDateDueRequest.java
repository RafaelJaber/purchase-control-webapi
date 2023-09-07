package br.psi.giganet.api.purchase.quotations.controller.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class InsertConditionDateDueRequest {

    @NotNull(message = "Data de vencimento não pode ser nula")
    private String dueDate;
}
