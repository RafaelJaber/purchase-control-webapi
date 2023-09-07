package br.psi.giganet.api.purchase.branch_offices.service;

import br.psi.giganet.api.purchase.branch_offices.model.BranchOffice;
import br.psi.giganet.api.purchase.branch_offices.repository.BranchOfficeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BranchOfficeService {

    @Autowired
    private BranchOfficeRepository branchOfficeRepository;

    public List<BranchOffice> findAll() {
        return branchOfficeRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public Optional<BranchOffice> insert(BranchOffice branchOffice) {
        return Optional.of(branchOfficeRepository.save(branchOffice));
    }

    public Optional<BranchOffice> update(Long id, BranchOffice branchOffice) {
        return this.findById(id)
                .map(saved -> {
                    saved.setCnpj(branchOffice.getCnpj());
                    saved.setName(branchOffice.getName());
                    saved.setShortName(branchOffice.getShortName());
                    saved.setAddress(branchOffice.getAddress());
                    saved.setTelephone(branchOffice.getTelephone());
                    saved.setStateRegistration(branchOffice.getStateRegistration());
                    return this.branchOfficeRepository.save(saved);
                });
    }

    public Optional<BranchOffice> findById(Long id) {
        return this.branchOfficeRepository.findById(id);
    }
}
