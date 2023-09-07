package br.psi.giganet.api.purchase.employees.controller;

import br.psi.giganet.api.purchase.config.exception.exception.ResourceNotFoundException;
import br.psi.giganet.api.purchase.config.security.model.Permission;
import br.psi.giganet.api.purchase.employees.adapter.EmployeeAdapter;
import br.psi.giganet.api.purchase.employees.controller.request.UpdatePermissionsRequest;
import br.psi.giganet.api.purchase.employees.controller.response.EmployeeProjection;
import br.psi.giganet.api.purchase.employees.controller.response.EmployeeResponse;
import br.psi.giganet.api.purchase.employees.controller.security.RoleRoot;
import br.psi.giganet.api.purchase.employees.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employees;

    @Autowired
    private EmployeeAdapter adapter;

    @PutMapping("/{id}/permissions")
    @RoleRoot
    public Optional<EmployeeProjection> updatePermissions(@RequestBody @Valid UpdatePermissionsRequest request) {
        return this.employees.updatePermissions(request.getId(), adapter.transform(request))
                .map(adapter::transform);
    }

    @GetMapping
    public List<EmployeeProjection> findByName(@RequestParam(defaultValue = "") String name) {
        return this.employees.findByNameContaining(name)
                .stream()
                .map(adapter::transformWithEmail)
                .collect(Collectors.toList());
    }

    @GetMapping("/permissions/{permission}")
    public List<EmployeeProjection> findByNameAndPermissions(
            @PathVariable String permission,
            @RequestParam(defaultValue = "") String name
    ) {
        return this.employees.findByNameContainingAndPermissions(name, new Permission(permission))
                .stream()
                .map(adapter::transform)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public EmployeeResponse findById(@PathVariable Long id) throws ResourceNotFoundException {
        return this.employees.findById(id)
                .map(adapter::transformToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado"));
    }

}
