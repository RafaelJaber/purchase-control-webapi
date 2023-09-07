package br.psi.giganet.api.purchase.employees.controller.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
public class UpdatePermissionsRequest {

    @NotNull(message = "Id não pode ser nulo")
    private Long id;

    private String name;
    private Set<String> permissions;

}
