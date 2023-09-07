package br.psi.giganet.api.purchase.quotation_approvals.adapter;

import br.psi.giganet.api.purchase.branch_offices.adapter.BranchOfficeAdapter;
import br.psi.giganet.api.purchase.employees.adapter.EmployeeAdapter;
import br.psi.giganet.api.purchase.quotation_approvals.controller.request.InsertQuotationApprovalRequest;
import br.psi.giganet.api.purchase.quotation_approvals.controller.request.UpdateQuotationApprovalRequest;
import br.psi.giganet.api.purchase.quotation_approvals.controller.response.QuotationApprovalProjection;
import br.psi.giganet.api.purchase.quotation_approvals.controller.response.QuotationApprovalProjectionWithoutQuotation;
import br.psi.giganet.api.purchase.quotation_approvals.controller.response.QuotationApprovalResponse;
import br.psi.giganet.api.purchase.quotation_approvals.model.QuotationApproval;
import br.psi.giganet.api.purchase.quotation_approvals.repository.ApprovalProjectionWithoutQuotation;
import br.psi.giganet.api.purchase.quotations.adapter.QuotationAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

@Component
public class QuotationApprovalAdapter {

    @Autowired
    private QuotationAdapter quotationAdapter;

    @Autowired
    private EmployeeAdapter employeeAdapter;

    @Autowired
    private BranchOfficeAdapter branchOfficeAdapter;

    public QuotationApproval transform(final InsertQuotationApprovalRequest request) {
        final QuotationApproval approval = new QuotationApproval();
        approval.setQuotation(quotationAdapter.create(request.getQuotation()));
        approval.setEvaluation(request.getEvaluation());
        approval.setNote(request.getNote());

        return approval;
    }

    public QuotationApproval transform(final UpdateQuotationApprovalRequest request) {
        final QuotationApproval approval = new QuotationApproval();
        approval.setId(request.getId());
        approval.setEvaluation(request.getEvaluation());
        approval.setNote(request.getNote());

        return approval;
    }

    @Transactional
    public QuotationApprovalProjectionWithoutQuotation transformWithoutQuotation(final ApprovalProjectionWithoutQuotation approval) {
        final QuotationApprovalProjectionWithoutQuotation response = new QuotationApprovalProjectionWithoutQuotation();
        response.setId(approval.getId());
        response.setDate(approval.getCreatedDate());
        response.setEvaluation(approval.getEvaluation());
        response.setQuotation(approval.getQuotation());
        response.setTotal(approval.getTotal());
        response.setRequester(employeeAdapter.transform(approval.getRequester()));
        response.setResponsible(approval.getResponsible() != null ? employeeAdapter.transform(approval.getResponsible()) : null);
        response.setBranchOffice(branchOfficeAdapter.transform(
                approval.getBranchOfficeId(),
                approval.getBranchOfficeName(),
                approval.getBranchOfficeShortName()));

        return response;
    }

    @Transactional
    public QuotationApprovalProjection transform(final QuotationApproval approval) {
        final QuotationApprovalProjection response = new QuotationApprovalProjection();
        response.setId(approval.getId());
        response.setDate(approval.getCreatedDate());
        response.setEvaluation(approval.getEvaluation());
        response.setQuotation(quotationAdapter.transform(approval.getQuotation()));
        response.setRequester(employeeAdapter.transform(approval.getQuotation().getResponsible()));
        response.setResponsible(approval.getResponsible() != null ? employeeAdapter.transform(approval.getResponsible()) : null);

        return response;
    }

    @Transactional
    public QuotationApprovalResponse transformToFullResponse(final QuotationApproval approval) {
        return transformToFullResponse(approval, false);
    }

    @Transactional
    public QuotationApprovalResponse transformToFullResponse(final QuotationApproval approval, boolean withTrace) {
        final QuotationApprovalResponse response = new QuotationApprovalResponse();
        response.setId(approval.getId());
        response.setDate(approval.getCreatedDate());
        response.setEvaluation(approval.getEvaluation());
        response.setQuotation(quotationAdapter.transformToFullResponse(approval.getQuotation(), withTrace ?
                QuotationAdapter.QuotedItemType.WITH_TRACE : QuotationAdapter.QuotedItemType.DEFAULT));
        response.setResponsible(approval.getResponsible() != null ? employeeAdapter.transform(approval.getResponsible()) : null);
        response.setRequester(employeeAdapter.transform(approval.getQuotation().getResponsible()));
        response.setNote(approval.getNote());

        return response;
    }

}
