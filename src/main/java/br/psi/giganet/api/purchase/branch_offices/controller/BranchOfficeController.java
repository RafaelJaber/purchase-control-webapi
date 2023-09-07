package br.psi.giganet.api.purchase.branch_offices.controller;

import br.psi.giganet.api.purchase.branch_offices.adapter.BranchOfficeAdapter;
import br.psi.giganet.api.purchase.branch_offices.controller.request.InsertBranchOfficeRequest;
import br.psi.giganet.api.purchase.branch_offices.controller.request.UpdateBranchOfficeRequest;
import br.psi.giganet.api.purchase.branch_offices.controller.response.BranchOfficeProjection;
import br.psi.giganet.api.purchase.branch_offices.controller.response.BranchOfficeResponse;
import br.psi.giganet.api.purchase.branch_offices.controller.security.RoleBranchOfficesRead;
import br.psi.giganet.api.purchase.branch_offices.controller.security.RoleBranchOfficesWrite;
import br.psi.giganet.api.purchase.branch_offices.service.BranchOfficeService;
import br.psi.giganet.api.purchase.config.exception.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/branch-offices")
public class BranchOfficeController {

    @Autowired
    private BranchOfficeService branchOfficeService;
    @Autowired
    private BranchOfficeAdapter branchOfficeAdapter;

    @GetMapping
    @RoleBranchOfficesRead
    public List<BranchOfficeProjection> findAll() {
        return this.branchOfficeService.findAll()
                .stream()
                .map(branchOfficeAdapter::transform)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @RoleBranchOfficesRead
    public BranchOfficeResponse findById(@PathVariable Long id) {
        return this.branchOfficeService.findById(id)
                .map(branchOfficeAdapter::transformToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Filial não encontrada"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RoleBranchOfficesWrite
    public BranchOfficeResponse insert(@RequestBody @Valid InsertBranchOfficeRequest request) {
        return this.branchOfficeService.insert(branchOfficeAdapter.transform(request))
                .map(branchOfficeAdapter::transformToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Taxa não encontrada"));
    }

    @PutMapping("/{id}")
    @RoleBranchOfficesWrite
    public BranchOfficeResponse update(@PathVariable Long id, @RequestBody @Valid UpdateBranchOfficeRequest request) {
        return this.branchOfficeService.update(id, branchOfficeAdapter.transform(request))
                .map(branchOfficeAdapter::transformToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Taxa não encontrada"));
    }

}
