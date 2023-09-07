package br.psi.giganet.api.purchase.employees.controller.response;

import lombok.Data;

import java.time.ZonedDateTime;
import java.util.Set;

@Data
public class EmployeeResponse {

    private Long id;
    private ZonedDateTime createdDate;
    private ZonedDateTime lastModifiedDate;
    private String email;
    private String name;
    private Set<String> permissions;

}
