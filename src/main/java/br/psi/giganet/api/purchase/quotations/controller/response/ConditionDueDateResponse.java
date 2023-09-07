package br.psi.giganet.api.purchase.quotations.controller.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ConditionDueDateResponse {

    private Long id;
    private LocalDate dueDate;

}
