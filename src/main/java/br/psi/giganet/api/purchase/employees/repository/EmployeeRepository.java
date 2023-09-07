package br.psi.giganet.api.purchase.employees.repository;

import br.psi.giganet.api.purchase.config.security.model.Permission;
import br.psi.giganet.api.purchase.employees.model.Employee;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);

    List<Employee> findByNameContainingIgnoreCase(String name, Sort sort);

    @Query("SELECT e FROM Employee e WHERE " +
            ":permission MEMBER OF e.permissions")
    List<Employee> findByPermission(Permission permission);

    @Query("SELECT e FROM Employee e WHERE " +
            "UPPER(e.name) LIKE CONCAT('%', UPPER(:name), '%') AND " +
            ":permission MEMBER OF e.permissions")
    List<Employee> findByNameContainingAndPermissions(String name, Permission permission);

    @Query("SELECT e FROM Employee e WHERE " +
            "UPPER(e.name) LIKE CONCAT('%', UPPER(:name), '%') AND " +
            ":permission MEMBER OF e.permissions")
    List<Employee> findByNameContainingAndPermissions(String name, Permission permission, Sort sort);

}
