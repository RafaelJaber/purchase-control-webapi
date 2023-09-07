package br.psi.giganet.api.purchase.suppliers.taxes.service;

import br.psi.giganet.api.purchase.suppliers.taxes.model.Tax;
import br.psi.giganet.api.purchase.suppliers.taxes.repository.TaxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaxService {

    @Autowired
    private TaxRepository taxRepository;

    public List<Tax> findAll() {
        return taxRepository.findAll(Sort.by(Sort.Direction.ASC, "stateFrom"));
    }

    public Optional<Tax> update(Long id, Tax tax) {
        return this.findById(id)
                .map(saved -> {
                    saved.setIcms(tax.getIcms());
                    return this.taxRepository.save(saved);
                });
    }

    public Optional<Tax> findByStateFrom(String state) {
        return this.taxRepository.findByStateFrom(state);
    }

    public Optional<Tax> findById(Long id) {
        return this.taxRepository.findById(id);
    }
}
