package br.psi.giganet.api.purchase.suppliers.service;

import br.psi.giganet.api.purchase.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.api.purchase.suppliers.adapter.SupplierAdapter;
import br.psi.giganet.api.purchase.suppliers.model.Supplier;
import br.psi.giganet.api.purchase.suppliers.repository.SupplierRepository;
import br.psi.giganet.api.purchase.suppliers.taxes.service.TaxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SupplierService {

    @Autowired
    private SupplierRepository suppliers;

    @Autowired
    private SupplierAdapter adapter;

    @Autowired
    private TaxService taxService;

    public Optional<Supplier> insert(Supplier supplier) {
        supplier.setTax(taxService.findByStateFrom(supplier.getAddress().getState())
                .orElseThrow(() -> new IllegalArgumentException("Não foram encontrados impostos para este estado")));
        return Optional.of(this.suppliers.save(supplier));
    }

    public Optional<Supplier> update(Long id, Supplier supplier) {
        return this.findById(id)
                .map(s -> {
                    supplier.setTax(taxService.findByStateFrom(supplier.getAddress().getState())
                            .orElseThrow(() -> new IllegalArgumentException("Não foram encontrados impostos para este estado")));
                    return this.suppliers.save(adapter.copyProperties(supplier, s));
                });
    }

    public Optional<Supplier> updateEmail(Long id, String email) {
        return this.findById(id)
                .map(s -> {
                    s.setEmail(email);
                    return this.suppliers.save(s);
                });
    }

    public List<Supplier> findAll() {
        return this.suppliers.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public Page<Supplier> findByNameContaining(String name, Integer page, Integer pageSize) {
        return this.suppliers.findByNameContainingIgnoreCase(name, PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "name")));
    }

    public Optional<Supplier> findById(Long id) {
        return this.suppliers.findById(id);
    }


}
