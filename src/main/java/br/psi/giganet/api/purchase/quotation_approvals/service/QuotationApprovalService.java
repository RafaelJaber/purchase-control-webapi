package br.psi.giganet.api.purchase.quotation_approvals.service;

import br.psi.giganet.api.purchase.common.notifications.service.NotificationService;
import br.psi.giganet.api.purchase.common.settings.service.SettingsService;
import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.api.purchase.employees.service.EmployeeService;
import br.psi.giganet.api.purchase.purchase_order.service.PurchaseOrderService;
import br.psi.giganet.api.purchase.quotation_approvals.model.QuotationApproval;
import br.psi.giganet.api.purchase.quotation_approvals.repository.ApprovalProjectionWithoutQuotation;
import br.psi.giganet.api.purchase.quotation_approvals.repository.QuotationApprovalRepository;
import br.psi.giganet.api.purchase.quotations.model.Quotation;
import br.psi.giganet.api.purchase.quotations.service.QuotationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class QuotationApprovalService {

    @Autowired
    private QuotationApprovalRepository quotationApprovals;

    @Autowired
    private QuotationService quotations;

    @Autowired
    private EmployeeService employees;

    @Autowired
    private PurchaseOrderService orders;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SettingsService settingsService;

    @Deprecated(forRemoval = true)
    public Optional<QuotationApproval> insert(QuotationApproval approval) {
        approval.setResponsible(employees.getCurrentLoggedEmployee()
                .orElseThrow(() -> new IllegalArgumentException("Responsável não encontrado")));
        approval.setQuotation(quotations.findById(approval.getQuotation().getId())
                .orElseThrow(() -> new IllegalArgumentException("Cotação não encontrada"))
        );
        return Optional.of(this.quotationApprovals.save(approval))
                .map(saved -> {
                    saved.getQuotation().setStatus(saved.getEvaluation());
                    quotations.updateStatus(saved.getQuotation());

                    if (saved.isApproved()) {
                        this.orders.insertFromQuotationApproval(saved);
                    }
                    return saved;
                });
    }

    public Optional<QuotationApproval> insertFromQuotation(final Quotation quotation) {
        if (quotation.getStatus() == ProcessStatus.APPROVED || quotation.getStatus() == ProcessStatus.REJECTED) {
            throw new IllegalArgumentException("Cotação já avaliada");
        } else if (quotationApprovals.findByQuotation(quotation).isPresent()) {
            throw new IllegalArgumentException("Um pedido de aprovação pendente já existe para esta cotação.");
        }

        final QuotationApproval approval = new QuotationApproval();
        approval.setQuotation(quotation);
        approval.setEvaluation(ProcessStatus.PENDING);

        QuotationApproval saved = this.quotationApprovals.save(approval);

        BigDecimal minAutoApproveQuantity = settingsService.getMinimalAmountToAutoApproveQuotation();
        if (saved.getQuotation().getTotal().compareTo(minAutoApproveQuantity) <= 0) {
            saved.setEvaluation(ProcessStatus.APPROVED);
            saved.setResponsible(saved.getQuotation().getResponsible());

            saved = this.quotationApprovals.save(saved);
            saved.getQuotation().setStatus(saved.getEvaluation());
            quotations.updateStatus(saved.getQuotation());

            this.orders.insertFromQuotationApproval(saved);

            notificationService.onEvaluateQuotationApproval(saved);

        } else {
            notificationService.onCreateQuotationApproval(saved);
        }

        return Optional.of(saved);
    }

    public Optional<QuotationApproval> evaluateHandler(final Long id, QuotationApproval evaluated) {
        return this.findById(id)
                .map(saved -> {
                    final List<ProcessStatus> finalizedStatuses = Arrays.asList(
                            ProcessStatus.APPROVED, ProcessStatus.REJECTED, ProcessStatus.CANCELED);
                    if (finalizedStatuses.contains(saved.getEvaluation())) {
                        throw new IllegalArgumentException("Não é possível alterar o status de uma avaliação já finalizada");
                    }

                    saved.setNote(evaluated.getNote());
                    saved.setEvaluation(evaluated.getEvaluation());
                    saved.setResponsible(employees.getCurrentLoggedEmployee()
                            .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado")));

                    saved = this.quotationApprovals.save(saved);
                    saved.getQuotation().setStatus(saved.getEvaluation());
                    quotations.updateStatus(saved.getQuotation());

                    if (saved.isApproved()) {
                        this.orders.insertFromQuotationApproval(saved);
                    }

                    notificationService.onEvaluateQuotationApproval(saved);

                    return saved;
                });
    }

    public Optional<QuotationApproval> cancelApprovalByQuotation(Quotation quotation) {
        return quotationApprovals.findByQuotation(quotation)
                .map(saved -> {
                    if (saved.getEvaluation() == ProcessStatus.APPROVED || saved.getEvaluation() == ProcessStatus.REJECTED) {
                        throw new IllegalArgumentException("Não é possível alterar o status de uma avaliação já finalizada");
                    }
                    saved.setEvaluation(ProcessStatus.CANCELED);

                    return this.quotationApprovals.save(saved);
                });
    }

    public List<QuotationApproval> findAll() {
        return this.quotationApprovals.findAll(Sort.by(Sort.Direction.DESC, "createdDate"));
    }

    public List<ApprovalProjectionWithoutQuotation> findAllWithEagerQuotationAndResponsible() {
        return this.quotationApprovals.findAllWithFetchEager(Sort.by(Sort.Direction.DESC, "createdDate"));
    }

    public List<QuotationApproval> findAllByEvaluation(ProcessStatus evaluation) {
        return this.quotationApprovals.findByEvaluation(evaluation, Sort.by(Sort.Direction.DESC, "createdDate"));
    }

    public Optional<QuotationApproval> findById(final Long id) {
        return this.quotationApprovals.findById(id);
    }

}
