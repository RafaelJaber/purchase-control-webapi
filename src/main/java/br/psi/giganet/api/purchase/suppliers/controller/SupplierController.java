package br.psi.giganet.api.purchase.suppliers.controller;

import br.psi.giganet.api.purchase.config.exception.exception.ResourceNotFoundException;
import br.psi.giganet.api.purchase.suppliers.adapter.SupplierAdapter;
import br.psi.giganet.api.purchase.suppliers.controller.request.SupplierRequest;
import br.psi.giganet.api.purchase.suppliers.controller.response.SupplierProjection;
import br.psi.giganet.api.purchase.suppliers.controller.response.SupplierProjectionAndTax;
import br.psi.giganet.api.purchase.suppliers.controller.response.SupplierResponse;
import br.psi.giganet.api.purchase.suppliers.controller.security.RoleSuppliersRead;
import br.psi.giganet.api.purchase.suppliers.controller.security.RoleSuppliersWrite;
import br.psi.giganet.api.purchase.suppliers.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("suppliers")
public class SupplierController {

    @Autowired
    private SupplierService suppliers;

    @Autowired
    private SupplierAdapter adapter;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RoleSuppliersWrite
    public Optional<SupplierProjection> insert(@RequestBody @Valid SupplierRequest supplier) {
        return this.suppliers.insert(adapter.transform(supplier)).map(adapter::transform);
    }


    @PutMapping("/{id}")
    @RoleSuppliersWrite
    public Optional<SupplierProjection> update(
            @PathVariable Long id,
            @RequestBody @Valid SupplierRequest supplier) {
        return this.suppliers.update(id, adapter.transform(supplier)).map(adapter::transform);
    }

    @GetMapping
    @RoleSuppliersRead
    public Page<SupplierProjection> findByName(
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return this.suppliers.findByNameContaining(name, page, pageSize)
                .map(adapter::transformProjectionAndEmail);
    }

    @GetMapping(params = { "withCPFAndCNPJ", "name", "page", "pageSize" })
    @RoleSuppliersRead
    public Page<SupplierProjection> findByNameWithCPFAndCNPJ(
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return this.suppliers.findByNameContaining(name, page, pageSize)
                .map(adapter::transformProjectionWithCPFAndCNPJ);
    }

    @GetMapping("/{id}")
    @RoleSuppliersRead
    public SupplierResponse findById(@PathVariable Long id) throws ResourceNotFoundException {
        return adapter.transformToResponse(
                this.suppliers.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Fornecedor nao encontrado")));
    }

    @GetMapping("/{id}/taxes")
    @RoleSuppliersRead
    public SupplierProjectionAndTax getTaxesById(@PathVariable Long id) throws ResourceNotFoundException {
        return adapter.transformProjectionAndTax(
                this.suppliers.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Fornecedor nao encontrado")));
    }

}
