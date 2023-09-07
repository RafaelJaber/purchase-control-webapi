package br.psi.giganet.api.purchase.approvals.controller;

import br.psi.giganet.api.purchase.approvals.adapter.ApprovalAdapter;
import br.psi.giganet.api.purchase.approvals.controller.request.ApprovalEvaluateRequest;
import br.psi.giganet.api.purchase.approvals.controller.response.AbstractApprovalItemResponse;
import br.psi.giganet.api.purchase.approvals.controller.response.ApprovalProjection;
import br.psi.giganet.api.purchase.approvals.controller.response.ApprovalResponse;
import br.psi.giganet.api.purchase.approvals.controller.security.RoleApprovalsRead;
import br.psi.giganet.api.purchase.approvals.controller.security.RoleApprovalsWrite;
import br.psi.giganet.api.purchase.approvals.service.ApprovalService;
import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.config.exception.exception.ResourceNotFoundException;
import br.psi.giganet.api.purchase.quotations.adapter.QuotationAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/approvals")
public class ApprovalController {

    @Autowired
    private ApprovalService approvals;

    @Autowired
    private ApprovalAdapter adapter;

    @Autowired
    private QuotationAdapter quotationAdapter;

    @GetMapping
    @RoleApprovalsRead
    public List<ApprovalProjection> findAll() {
        return this.approvals.findAll()
                .stream()
                .map(adapter::transform)
                .collect(Collectors.toList());
    }

    @GetMapping("/statuses")
    @RoleApprovalsRead
    public List<ApprovalProjection> findByStatus(@RequestParam List<ProcessStatus> statuses) {
        return this.approvals.findByStatus(statuses)
                .stream()
                .map(adapter::transform)
                .collect(Collectors.toList());
    }

    @GetMapping("/quotation-available")
    @RoleApprovalsRead
    public List<ApprovalProjection> findApprovalsAvailableToQuotation() {
        return this.approvals.findApprovalsAvailableToQuotation()
                .stream()
                .map(adapter::transform)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @RoleApprovalsRead
    public ApprovalResponse findById(@PathVariable Long id) {
        return this.approvals.findById(id)
                .map(adapter::transformToFullResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Aprovação não encontrada"));
    }

    @GetMapping(path = "/{id}", params = {"withUnits"})
    @RoleApprovalsRead
    public ApprovalResponse findWithApprovalAndAvailableUnitsById(@PathVariable Long id) {
        return this.approvals.findById(id)
                .map(a -> adapter.transformToFullResponse(a, ApprovalAdapter.ApprovalItemType.WITH_AVAILABLE_UNITS))
                .orElseThrow(() -> new ResourceNotFoundException("Aprovação não encontrada"));
    }

    @GetMapping("/quotation-available/items")
    @RoleApprovalsRead
    public List<AbstractApprovalItemResponse> findAllApprovalItemsAvailableToQuotation(
            @RequestParam(required = false) Long ignoredQuotation) {
        var list = ignoredQuotation != null ?
                this.approvals.findAllApprovalItemsAvailableToQuotation(quotationAdapter.create(ignoredQuotation)) :
                this.approvals.findAllApprovalItemsAvailableToQuotation();

        return list.stream()
                .map(adapter::transformWithAvailableUnitsAndApprovalProjection)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}/items")
    @RoleApprovalsRead
    public List<AbstractApprovalItemResponse> findApprovedItemsByApproval(@PathVariable Long id) {
        return this.approvals.findApprovalItemsAvailableToQuotationByApproval(id)
                .stream()
                .map(adapter::transformWithAvailableUnits)
                .collect(Collectors.toList());
    }

    @GetMapping("/items/{id}")
    @RoleApprovalsRead
    public AbstractApprovalItemResponse findByItemId(@PathVariable Long id) {
        return this.approvals.findItemById(id)
                .map(adapter::transformWithAvailableUnits)
                .orElseThrow(() -> new ResourceNotFoundException("Item de aprovação não encontrado"));
    }

    @PostMapping("/items/{id}/discard")
    @PreAuthorize("hasAnyRole({ 'ROLE_APPROVALS_WRITE', 'ROLE_QUOTATIONS_WRITE', 'ROLE_ROOT' })")
    public AbstractApprovalItemResponse markItemAsDiscarded(@PathVariable Long id) {
        return this.approvals.markItemAsDiscarded(adapter.createItem(id))
                .map(adapter::transform)
                .orElseThrow(() -> new ResourceNotFoundException("Item de aprovação não encontrado"));
    }

    @PutMapping("/{id}/evaluate")
    @RoleApprovalsWrite
    public ApprovalResponse evaluateHandler(@PathVariable Long id, @RequestBody @Valid ApprovalEvaluateRequest evaluate) {
        return this.approvals.evaluateHandler(id, adapter.transform(evaluate))
                .map(adapter::transformToFullResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Aprovação não encontrada"));
    }

}
