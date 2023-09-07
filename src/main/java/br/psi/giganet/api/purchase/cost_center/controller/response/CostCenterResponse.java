package br.psi.giganet.api.purchase.cost_center.controller.response;

import lombok.Data;

@Data
public class CostCenterResponse {

    private Long id;
    private String name;
    private String description;
}
