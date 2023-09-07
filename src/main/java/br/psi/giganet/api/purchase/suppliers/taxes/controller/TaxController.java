package br.psi.giganet.api.purchase.suppliers.taxes.controller;

import br.psi.giganet.api.purchase.config.exception.exception.ResourceNotFoundException;
import br.psi.giganet.api.purchase.suppliers.controller.security.RoleSuppliersRead;
import br.psi.giganet.api.purchase.suppliers.controller.security.RoleSuppliersWrite;
import br.psi.giganet.api.purchase.suppliers.taxes.adapter.TaxAdapter;
import br.psi.giganet.api.purchase.suppliers.taxes.controller.request.TaxRequest;
import br.psi.giganet.api.purchase.suppliers.taxes.controller.response.TaxResponse;
import br.psi.giganet.api.purchase.suppliers.taxes.service.TaxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/suppliers/taxes")
public class TaxController {

    @Autowired
    private TaxService taxService;
    @Autowired
    private TaxAdapter taxAdapter;

    @GetMapping
    @RoleSuppliersRead
    public List<TaxResponse> findAll() {
        return this.taxService.findAll()
                .stream()
                .map(taxAdapter::transform)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @RoleSuppliersRead
    public TaxResponse findById(@PathVariable Long id) {
        return this.taxService.findById(id)
                .map(taxAdapter::transform)
                .orElseThrow(() -> new ResourceNotFoundException("Taxa não encontrada"));
    }

    @PutMapping("/{id}")
    @RoleSuppliersWrite
    public TaxResponse update(@PathVariable Long id, @RequestBody @Valid TaxRequest request) {
        return this.taxService.update(id, taxAdapter.transform(request))
                .map(taxAdapter::transform)
                .orElseThrow(() -> new ResourceNotFoundException("Taxa não encontrada"));
    }

}
