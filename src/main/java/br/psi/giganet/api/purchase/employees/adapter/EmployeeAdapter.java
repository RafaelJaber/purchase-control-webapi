package br.psi.giganet.api.purchase.employees.adapter;

import br.psi.giganet.api.purchase.config.security.model.Permission;
import br.psi.giganet.api.purchase.employees.controller.request.InsertEmployeeRequest;
import br.psi.giganet.api.purchase.employees.controller.request.UpdatePermissionsRequest;
import br.psi.giganet.api.purchase.employees.controller.response.EmployeeProjection;
import br.psi.giganet.api.purchase.employees.controller.response.EmployeeProjectionWithEmail;
import br.psi.giganet.api.purchase.employees.controller.response.EmployeeResponse;
import br.psi.giganet.api.purchase.employees.model.Employee;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;

@Component
public class EmployeeAdapter {

    public Employee create(Long id) {
        Employee e = new Employee();
        e.setId(id);
        return e;
    }

    public Employee transform(InsertEmployeeRequest request) {
        Employee e = new Employee();
        e.setName(request.getName());
        e.setPassword(request.getPassword());
        e.setEmail(request.getEmail());
        return e;
    }

    public Employee transform(UpdatePermissionsRequest request) {
        Employee e = new Employee();
        e.setId(request.getId());
        e.setName(request.getName());
        e.setPermissions(
                request.getPermissions()
                        .stream().map(Permission::new)
                        .collect(Collectors.toSet()));
        return e;
    }

    public EmployeeResponse transformToResponse(Employee employee) {
        EmployeeResponse response = new EmployeeResponse();
        response.setId(employee.getId());
        response.setCreatedDate(employee.getCreatedDate());
        response.setLastModifiedDate(employee.getLastModifiedDate());
        response.setEmail(employee.getEmail());
        response.setName(employee.getName());
        response.setPermissions(
                employee.getPermissions() != null ?
                        employee.getPermissions().stream().map(Permission::getName).collect(Collectors.toSet()) :
                        Collections.emptySet());

        return response;
    }

    public EmployeeProjection transform(Long id, String name) {
        EmployeeProjection projection = new EmployeeProjection();
        projection.setId(id);
        projection.setName(name);
        return projection;
    }

    public EmployeeProjection transform(Employee employee) {
        EmployeeProjection projection = new EmployeeProjection();
        projection.setId(employee.getId());
        projection.setName(employee.getName());
        return projection;
    }

    public EmployeeProjection transformWithEmail(Employee employee) {
        EmployeeProjectionWithEmail projection = new EmployeeProjectionWithEmail();
        projection.setName(employee.getName());
        projection.setId(employee.getId());
        projection.setEmail(employee.getEmail());
        return projection;
    }

    public Employee createDefaultUser(String name, String email, String password) {
        Employee e = new Employee();
        e.setName(name);
        e.setPassword(new BCryptPasswordEncoder().encode(password));
        e.setEmail(email);
        e.setPermissions(new HashSet<>(Collections.singletonList(new Permission("ROLE_ADMIN"))));

        return e;
    }


}
