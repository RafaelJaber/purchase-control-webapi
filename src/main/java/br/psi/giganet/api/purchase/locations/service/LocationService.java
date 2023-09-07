package br.psi.giganet.api.purchase.locations.service;

import br.psi.giganet.api.purchase.branch_offices.model.BranchOffice;
import br.psi.giganet.api.purchase.branch_offices.service.BranchOfficeService;
import br.psi.giganet.api.purchase.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.api.purchase.locations.model.Location;
import br.psi.giganet.api.purchase.locations.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class LocationService {

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private BranchOfficeService branchOfficeService;

    public List<Location> findAll() {
        return locationRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public Optional<Location> findById(Long id) {
        return locationRepository.findById(id);
    }

    public List<Location> findByBranchOffice(BranchOffice branchOffice) {
        return locationRepository.findByBranchOffice(branchOffice);
    }

    public Optional<Location> insert(Location location) {
        location.setBranchOffice(branchOfficeService.findById(location.getBranchOffice().getId())
                .orElseThrow(() -> new IllegalArgumentException("Filial não encontrada")));
        return Optional.of(locationRepository.save(location));
    }

    @Transactional
    public Optional<Location> update(Long id, Location location) {
        return locationRepository.findById(id)
                .map(saved -> {
                    saved.setName(location.getName());
                    saved.setDescription(location.getDescription());
                    saved.setBranchOffice(branchOfficeService.findById(location.getBranchOffice().getId())
                            .orElseThrow(() -> new IllegalArgumentException("Filial não encontrada")));

                    return locationRepository.save(saved);
                });
    }

}
