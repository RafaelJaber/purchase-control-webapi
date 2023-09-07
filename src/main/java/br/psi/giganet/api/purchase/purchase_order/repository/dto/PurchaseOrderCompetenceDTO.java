package br.psi.giganet.api.purchase.purchase_order.repository.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PurchaseOrderCompetenceDTO {

    private Long id;
    private LocalDate date;
    private Long orderId;

}
