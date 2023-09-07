package br.psi.giganet.api.purchase.cost_center.service;

import br.psi.giganet.api.purchase.cost_center.model.CostCenter;
import br.psi.giganet.api.purchase.cost_center.repository.CostCenterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class CostCenterService {

    @Autowired
    private CostCenterRepository costCenterRepository;

    public List<CostCenter> findAll() {
        return costCenterRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public Optional<CostCenter> findById(Long id) {
        return costCenterRepository.findById(id);
    }

    public Optional<CostCenter> insert(CostCenter costCenter) {
        return Optional.of(costCenterRepository.save(costCenter));
    }

    @Transactional
    public Optional<CostCenter> update(Long id, CostCenter costCenter) {
        return costCenterRepository.findById(id)
                .map(saved -> {
                    saved.setName(costCenter.getName());
                    saved.setDescription(costCenter.getDescription());

                    return costCenterRepository.save(saved);
                });
    }
}
