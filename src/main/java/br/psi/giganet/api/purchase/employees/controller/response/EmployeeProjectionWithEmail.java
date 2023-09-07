package br.psi.giganet.api.purchase.employees.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeProjectionWithEmail extends EmployeeProjection {

    private String email;

}
