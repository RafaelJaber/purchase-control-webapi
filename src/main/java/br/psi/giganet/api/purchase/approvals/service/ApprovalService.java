package br.psi.giganet.api.purchase.approvals.service;

import br.psi.giganet.api.purchase.approvals.model.Approval;
import br.psi.giganet.api.purchase.approvals.model.ApprovalItem;
import br.psi.giganet.api.purchase.approvals.repository.ApprovalItemRepository;
import br.psi.giganet.api.purchase.approvals.repository.ApprovalRepository;
import br.psi.giganet.api.purchase.approvals.repository.projections.AvailableApprovalItem;
import br.psi.giganet.api.purchase.approvals.repository.projections.AvailableItemWithApprovalAndCostCenterAndBranchOffice;
import br.psi.giganet.api.purchase.common.notifications.service.NotificationService;
import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.common.utils.statuses.StatusesUtil;
import br.psi.giganet.api.purchase.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.api.purchase.config.security.model.Permission;
import br.psi.giganet.api.purchase.employees.service.EmployeeService;
import br.psi.giganet.api.purchase.purchase_requests.model.PurchaseRequest;
import br.psi.giganet.api.purchase.purchase_requests.service.PurchaseRequestService;
import br.psi.giganet.api.purchase.quotations.model.Quotation;
import br.psi.giganet.api.purchase.quotations.service.QuotationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ApprovalService {

    @Autowired
    private ApprovalRepository approvals;

    @Autowired
    private ApprovalItemRepository approvalItems;

    @Autowired
    private PurchaseRequestService purchaseRequestService;

    @Autowired
    private QuotationService quotationService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private NotificationService notificationService;


    public Optional<Approval> insertFromPurchaseRequest(PurchaseRequest request) {
        if (request.getStatus().equals(ProcessStatus.REJECTED) ||
                request.getStatus().equals(ProcessStatus.APPROVED)) {
            throw new IllegalArgumentException("Esta solicitação ja foi avaliada e não pode ser mais alterada");
        } else if (approvals.findByRequest(request).stream()
                .anyMatch(a -> StatusesUtil.isPending(a.getItems()))) {
            throw new IllegalArgumentException("Esta solicitação possui uma aprovação pendente");
        }

        Approval approval = new Approval();
        approval.setRequest(request);
        approval.setStatus(ProcessStatus.PENDING);
        approval.setResponsible(request.getResponsible());
        approval.setItems(request.getItems()
                .stream()
                .filter(item -> !item.getStatus().equals(ProcessStatus.APPROVED) &&
                        !item.getStatus().equals(ProcessStatus.REJECTED))
                .peek(item -> item.setStatus(ProcessStatus.PENDING))
                .map(item -> new ApprovalItem(approval, item, null, ProcessStatus.PENDING, Boolean.FALSE))
                .collect(Collectors.toList())
        );

        if (approval.getItems() == null || approval.getItems().isEmpty()) {
            throw new IllegalArgumentException("Esta solicitação não possui itens pendentes a serem aprovados");
        }

        final Approval saved = this.approvals.save(approval);
        if (request.getRequester().hasRole(new Permission("ROLE_APPROVALS_WRITE"))) {
            saved.setResponsible(request.getRequester());
            saved.getItems().forEach(approvalItem -> {
                approvalItem.getItem().setStatus(ProcessStatus.APPROVED);
                approvalItem.setEvaluation(ProcessStatus.APPROVED);
            });
            approval.setStatus(ProcessStatus.APPROVED);
            this.approvals.save(saved);
            notificationService.onEvaluateApproval(saved);
            this.purchaseRequestService.updatePurchaseRequestStatus(saved.getRequest().getId(), ProcessStatus.APPROVED);
        }

        return Optional.of(saved);
    }

    public Optional<Approval> evaluateHandler(Long idApproval, Approval evaluated) {
        return this.findById(idApproval)
                .map(approval -> {
                    if (StatusesUtil.isApproved(approval.getItems()) || StatusesUtil.isRejected(approval.getItems())) {
                        throw new IllegalArgumentException("Não é possível alterar uma aprovação já finalizada. Código " + approval.getId());
                    }

                    approval.setNote(evaluated.getNote());
                    evaluated.getItems().forEach(item -> {
                        final int index = approval.getItems().indexOf(item);
                        if (index == -1) {
                            throw new IllegalArgumentException("Item de aprovação não encontrado. Código: " + item.getId());
                        }
                        approval.getItems()
                                .get(index)
                                .getItem()
                                .setStatus(item.getEvaluation());

                        approval.getItems()
                                .get(index)
                                .setEvaluation(item.getEvaluation());
                    });

                    approval.setResponsible(this.employeeService.getCurrentLoggedEmployee()
                            .orElseThrow(() -> new IllegalArgumentException("Responsável não encontrado")));

                    if (!approval.getResponsible().hasAnyRole(Arrays.asList(
                            new Permission("ROLE_APPROVALS_WRITE"),
                            new Permission("ROLE_ROOT")))) {
                        throw new IllegalArgumentException("O responsável associado não possui permissões suficientes para realizar a aprovação");
                    }

                    approval.setStatus(StatusesUtil.getStatus(approval.getItems()));

                    final Approval savedApproval = this.approvals.save(approval);
                    if (!StatusesUtil.isPending(savedApproval.getItems())) {
                        this.purchaseRequestService
                                .updatePurchaseRequestStatus(savedApproval.getRequest().getId(),
                                        StatusesUtil.getStatus(approval.getRequest().getItems()));
                    }

                    notificationService.onEvaluateApproval(savedApproval);
                    return savedApproval;
                });
    }

    public Optional<ApprovalItem> markItemAsDiscarded(ApprovalItem item) {
        return this.findItemById(item.getId())
                .map(approvalItem -> {
                    if (!approvalItem.isApproved()) {
                        throw new IllegalArgumentException("Este item não pode ser descartado pois não foi aprovado.");
                    }
                    approvalItem.setIsDiscarded(Boolean.TRUE);

                    return approvalItems.save(approvalItem);
                });
    }

    public List<Approval> findApprovalsAvailableToQuotation() {
        return this.approvals.findAvailableToQuotation();
    }

    public List<AvailableApprovalItem> findApprovalItemsAvailableToQuotationByApproval(Long id) {
        return this.approvalItems.findAvailableByApproval(id);
    }

    public List<AvailableItemWithApprovalAndCostCenterAndBranchOffice> findAllApprovalItemsAvailableToQuotation() {
        return this.approvalItems.findAllAvailable(PageRequest.of(0, 1000));
    }

    public List<AvailableItemWithApprovalAndCostCenterAndBranchOffice> findAllApprovalItemsAvailableToQuotation(
            Quotation ignoredQuotation) {
        return this.approvalItems.findAllAvailableIgnoringQuotations(
                quotationService.findById(ignoredQuotation.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Cotação não encontrada")),
                PageRequest.of(0, 1000));
    }

    public List<Approval> findAll() {
        return this.approvals.findAll(Sort.by(Sort.Direction.DESC, "createdDate"));
    }

    public List<Approval> findByStatus(List<ProcessStatus> statuses) {
        return this.approvals.findByStatus(statuses, PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "createdDate")));
    }

    public Optional<Approval> findById(Long id) {
        return this.approvals.findById(id);
    }

    public Optional<ApprovalItem> findItemById(final Long id) {
        return this.approvalItems.findById(id);
    }
}
