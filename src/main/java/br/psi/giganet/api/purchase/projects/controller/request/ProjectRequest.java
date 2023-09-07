package br.psi.giganet.api.purchase.projects.controller.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class ProjectRequest {

    @NotEmpty(message = "Nome é obrigatorio")
    private String name;
    private String description;

}
