package br.psi.giganet.api.purchase.locations.controller;

import br.psi.giganet.api.purchase.branch_offices.adapter.BranchOfficeAdapter;
import br.psi.giganet.api.purchase.config.exception.exception.ResourceNotFoundException;
import br.psi.giganet.api.purchase.locations.adapter.LocationAdapter;
import br.psi.giganet.api.purchase.locations.controller.request.InsertLocationRequest;
import br.psi.giganet.api.purchase.locations.controller.response.LocationProjection;
import br.psi.giganet.api.purchase.locations.controller.response.LocationResponse;
import br.psi.giganet.api.purchase.locations.controller.security.RoleLocationsRead;
import br.psi.giganet.api.purchase.locations.controller.security.RoleLocationsWrite;
import br.psi.giganet.api.purchase.locations.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/locations")
public class LocationController {

    @Autowired
    private LocationService locationService;

    @Autowired
    private LocationAdapter locationAdapter;

    @Autowired
    private BranchOfficeAdapter branchOfficeAdapter;

    @GetMapping
    @RoleLocationsRead
    public List<LocationProjection> findAll() {
        return locationService.findAll()
                .stream()
                .map(locationAdapter::transform)
                .collect(Collectors.toList());
    }

    @GetMapping("/branch-offices/{office}")
    @RoleLocationsRead
    public List<LocationProjection> findByBranchOffice(@PathVariable Long office) {
        return locationService.findByBranchOffice(branchOfficeAdapter.create(office))
                .stream()
                .map(locationAdapter::transform)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @RoleLocationsRead
    public LocationResponse findById(@PathVariable Long id) {
        return locationService.findById(id)
                .map(locationAdapter::transformToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Localização não encontrada"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RoleLocationsWrite
    public LocationResponse insert(@Valid @RequestBody InsertLocationRequest request) {
        return locationService.insert(locationAdapter.transform(request))
                .map(locationAdapter::transformToResponse)
                .orElseThrow(() -> new RuntimeException("Não foi possível cadastrar esta localização"));
    }

    @PutMapping("/{id}")
    @RoleLocationsWrite
    public LocationResponse update(
            @PathVariable Long id,
            @Valid @RequestBody InsertLocationRequest request) {
        return locationService.update(id, locationAdapter.transform(request))
                .map(locationAdapter::transformToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Localização não encontrada"));
    }
}
