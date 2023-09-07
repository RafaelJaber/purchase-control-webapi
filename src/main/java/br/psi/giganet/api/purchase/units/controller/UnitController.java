package br.psi.giganet.api.purchase.units.controller;

import br.psi.giganet.api.purchase.config.exception.exception.ResourceNotFoundException;
import br.psi.giganet.api.purchase.units.adapter.UnitAdapter;
import br.psi.giganet.api.purchase.units.controller.request.UnitRequest;
import br.psi.giganet.api.purchase.units.controller.response.UnitProjection;
import br.psi.giganet.api.purchase.units.controller.response.UnitResponse;
import br.psi.giganet.api.purchase.units.controller.security.RoleUnitsRead;
import br.psi.giganet.api.purchase.units.controller.security.RoleUnitsWrite;
import br.psi.giganet.api.purchase.units.service.UnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("units")
public class UnitController {

    @Autowired
    private UnitService unitService;

    @Autowired
    private UnitAdapter unitAdapter;

    @GetMapping
    @RoleUnitsRead
    public List<UnitProjection> findAll() {
        return unitService.findAll()
                .stream()
                .map(unitAdapter::transform)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @RoleUnitsRead
    public UnitResponse findById(@PathVariable Long id) {
        return unitService.findById(id)
                .map(unitAdapter::transformToFullResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Unidade não encontrada"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RoleUnitsWrite
    public UnitResponse insert(@Valid @RequestBody UnitRequest request) {
        return unitService.insert(unitAdapter.transform(request))
                .map(unitAdapter::transformToFullResponse)
                .orElseThrow(() -> new RuntimeException("Não foi possível cadastrar esta unidade"));
    }

    @PutMapping("/{id}")
    @RoleUnitsWrite
    public UnitResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UnitRequest request) {
        return unitService.update(id, unitAdapter.transform(request))
                .map(unitAdapter::transformToFullResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Unidade não encontrada"));
    }
}
