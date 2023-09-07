package br.psi.giganet.api.purchase.employees.service;

import br.psi.giganet.api.purchase.config.security.model.Permission;
import br.psi.giganet.api.purchase.config.security.service.AuthUtilsService;
import br.psi.giganet.api.purchase.employees.model.Employee;
import br.psi.giganet.api.purchase.employees.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employees;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthUtilsService authUtilsService;

    public Optional<Employee> insert(Employee employee) {
        employee.setPassword(passwordEncoder.encode(employee.getPassword()));
        return Optional.of(this.employees.save(employee));
    }

    public List<Employee> findAll() {
        return this.employees.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public Optional<Employee> findById(Long id) {
        return this.employees.findById(id);
    }

    public List<Employee> findByNameContaining(String name) {
        return this.employees.findByNameContainingIgnoreCase(name, Sort.by(Sort.Direction.ASC, "name"));
    }

    @Transactional
    public List<Employee> findByNameContainingAndPermissions(String name, Permission permission) {
        return this.employees.findByNameContainingAndPermissions(name, permission);
    }

    public List<Employee> findByPermission(Permission permission) {
        return this.employees.findByPermission(permission);
    }

    public Optional<Employee> getCurrentLoggedEmployee() {
        return this.authUtilsService.getCurrentUsername()
                .flatMap(username -> this.employees.findByEmail(username));
    }

    public Boolean isUserRootCurrentLogged() {
        Employee employee = new Employee();
        employee.setPermissions(this.authUtilsService.getCurrentAuthorities());

        return employee.isRoot();
    }

    public Optional<Employee> updatePermissions(Long id, Employee employee) {
        return employees.findById(id)
                .map(e -> {
                    e.setName(employee.getName());
                    e.setPermissions(employee.getPermissions());
                    return employees.save(e);
                });
    }

}
