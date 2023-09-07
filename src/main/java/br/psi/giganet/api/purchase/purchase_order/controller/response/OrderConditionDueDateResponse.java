package br.psi.giganet.api.purchase.purchase_order.controller.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class OrderConditionDueDateResponse {

    private Long id;
    private LocalDate dueDate;

}
