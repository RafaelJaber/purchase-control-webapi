package br.psi.giganet.api.purchase.cost_center.controller;

import br.psi.giganet.api.purchase.config.exception.exception.ResourceNotFoundException;
import br.psi.giganet.api.purchase.cost_center.adapter.CostCenterAdapter;
import br.psi.giganet.api.purchase.cost_center.controller.request.CostCenterRequest;
import br.psi.giganet.api.purchase.cost_center.controller.response.CostCenterResponse;
import br.psi.giganet.api.purchase.cost_center.controller.security.RoleCostCenterRead;
import br.psi.giganet.api.purchase.cost_center.controller.security.RoleCostCenterWrite;
import br.psi.giganet.api.purchase.cost_center.service.CostCenterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("cost-centers")
public class CostCenterController {

    @Autowired
    private CostCenterService costCenterService;

    @Autowired
    private CostCenterAdapter costCenterAdapter;

    @GetMapping
    @RoleCostCenterRead
    public List<CostCenterResponse> findAll() {
        return costCenterService.findAll()
                .stream()
                .map(costCenterAdapter::transform)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @RoleCostCenterRead
    public CostCenterResponse findById(@PathVariable Long id) {
        return costCenterService.findById(id)
                .map(costCenterAdapter::transform)
                .orElseThrow(() -> new ResourceNotFoundException("Centro de custo não encontrado"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RoleCostCenterWrite
    public CostCenterResponse insert(@Valid @RequestBody CostCenterRequest request) {
        return costCenterService.insert(costCenterAdapter.transform(request))
                .map(costCenterAdapter::transform)
                .orElseThrow(() -> new RuntimeException("Não foi possível cadastrar este centro de custo"));
    }

    @PutMapping("/{id}")
    @RoleCostCenterWrite
    public CostCenterResponse update(
            @PathVariable Long id,
            @Valid @RequestBody CostCenterRequest request) {
        return costCenterService.update(id, costCenterAdapter.transform(request))
                .map(costCenterAdapter::transform)
                .orElseThrow(() -> new ResourceNotFoundException("Centro de custo não encontrado"));
    }
}
