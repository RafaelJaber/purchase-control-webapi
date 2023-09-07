package br.psi.giganet.api.purchase.cost_center.controller.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class CostCenterRequest {

    @NotEmpty(message = "Nome é obrigatorio")
    private String name;
    private String description;

}
