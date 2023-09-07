package br.psi.giganet.api.purchase.quotation_approvals.controller;

import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.api.purchase.config.exception.exception.ResourceNotFoundException;
import br.psi.giganet.api.purchase.quotation_approvals.adapter.QuotationApprovalAdapter;
import br.psi.giganet.api.purchase.quotation_approvals.controller.request.InsertQuotationApprovalRequest;
import br.psi.giganet.api.purchase.quotation_approvals.controller.request.UpdateQuotationApprovalRequest;
import br.psi.giganet.api.purchase.quotation_approvals.controller.response.QuotationApprovalProjection;
import br.psi.giganet.api.purchase.quotation_approvals.controller.response.QuotationApprovalProjectionWithoutQuotation;
import br.psi.giganet.api.purchase.quotation_approvals.controller.response.QuotationApprovalResponse;
import br.psi.giganet.api.purchase.quotation_approvals.model.QuotationApproval;
import br.psi.giganet.api.purchase.quotation_approvals.service.QuotationApprovalService;
import br.psi.giganet.api.purchase.quotations.controller.security.RoleQuotationsRead;
import br.psi.giganet.api.purchase.quotations.controller.security.RoleQuotationsWrite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("quotation-approvals")
public class QuotationApprovalController {

    @Autowired
    private QuotationApprovalService approvals;

    @Autowired
    private QuotationApprovalAdapter adapter;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RoleQuotationsWrite
    @Deprecated(forRemoval = true)
    public QuotationApprovalResponse insert(@RequestBody @Valid InsertQuotationApprovalRequest request) {
        return this.approvals.insert(this.adapter.transform(request))
                .map(adapter::transformToFullResponse)
                .orElseThrow(() -> new IllegalArgumentException("Não foi possível salvar sua aprovação de cotação"));
    }

    @PutMapping("/{id}")
    @RoleQuotationsWrite
    public QuotationApprovalResponse evaluateHandler(
            @PathVariable Long id,
            @RequestBody @Valid UpdateQuotationApprovalRequest request
    ) {
        return this.approvals.evaluateHandler(id, this.adapter.transform(request))
                .map(adapter::transformToFullResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Aprovação de cotação não encontrada"));
    }

    @GetMapping
    @RoleQuotationsRead
    public List<QuotationApprovalProjection> findAll(
            @RequestParam(required = false) ProcessStatus evaluation) {
        List<QuotationApproval> result = evaluation != null ?
                this.approvals.findAllByEvaluation(evaluation) :
                this.approvals.findAll();

        return result.stream()
                .map(adapter::transform)
                .collect(Collectors.toList());
    }

    @GetMapping(params = {"withoutQuotation"})
    @RoleQuotationsRead
    public List<QuotationApprovalProjectionWithoutQuotation> findAllWithoutQuotation( ) {
        return this.approvals.findAllWithEagerQuotationAndResponsible().stream()
                .map(adapter::transformWithoutQuotation)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @RoleQuotationsRead
    public QuotationApprovalResponse findById(@PathVariable Long id) {
        return this.approvals.findById(id)
                .map(adapter::transformToFullResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Aprovação de cotação não encontrada"));
    }

    @GetMapping("/trace/{id}")
    @RoleQuotationsRead
    public QuotationApprovalResponse findByIdWithTrace(@PathVariable Long id) {
        return this.approvals.findById(id)
                .map(ap -> adapter.transformToFullResponse(ap, true))
                .orElseThrow(() -> new ResourceNotFoundException("Aprovação de cotação não encontrada"));
    }
}
