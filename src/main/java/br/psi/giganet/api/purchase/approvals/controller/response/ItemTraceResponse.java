package br.psi.giganet.api.purchase.approvals.controller.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ItemTraceResponse {

    private String unit;
    private Double value;

}
