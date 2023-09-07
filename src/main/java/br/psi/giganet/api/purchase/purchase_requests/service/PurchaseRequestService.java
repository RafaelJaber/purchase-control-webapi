package br.psi.giganet.api.purchase.purchase_requests.service;

import br.psi.giganet.api.purchase.approvals.service.ApprovalService;
import br.psi.giganet.api.purchase.branch_offices.service.BranchOfficeService;
import br.psi.giganet.api.purchase.common.reports.pdf.services.PdfReportService;
import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.common.utils.statuses.StatusesUtil;
import br.psi.giganet.api.purchase.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.api.purchase.cost_center.service.CostCenterService;
import br.psi.giganet.api.purchase.employees.model.Employee;
import br.psi.giganet.api.purchase.employees.service.EmployeeService;
import br.psi.giganet.api.purchase.products.service.ProductService;
import br.psi.giganet.api.purchase.purchase_requests.model.PurchaseRequest;
import br.psi.giganet.api.purchase.purchase_requests.model.PurchaseRequestItem;
import br.psi.giganet.api.purchase.purchase_requests.repository.PurchaseRequestItemsRepository;
import br.psi.giganet.api.purchase.purchase_requests.repository.PurchaseRequestRepository;
import br.psi.giganet.api.purchase.units.service.UnitService;
import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PurchaseRequestService {

    @Autowired
    private PurchaseRequestRepository purchaseRequests;
    @Autowired
    private PurchaseRequestItemsRepository purchaseRequestItemsRepository;
    @Autowired
    private ProductService products;
    @Autowired
    private EmployeeService employees;
    @Autowired
    private ApprovalService approvalService;
    @Autowired
    private CostCenterService costCenterService;
    @Autowired
    private UnitService unitService;
    @Autowired
    private PdfReportService pdfReportService;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private BranchOfficeService branchOfficeService;

    @Transactional
    public Optional<PurchaseRequest> insert(final PurchaseRequest request) {
        Employee responsible = this.employees.findById(request.getResponsible().getId())
                .orElseThrow(() -> new IllegalArgumentException("Responsável não encontrado"));
        List<PurchaseRequestItem> items = request.getItems()
                .stream()
                .peek(p -> {
                    p.setProduct(this.products
                            .findById(p.getProduct().getId())
                            .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado")));
                    p.setStatus(ProcessStatus.PENDING);
                    p.setUnit(unitService.findById(p.getUnit().getId())
                            .orElseThrow(() -> new IllegalArgumentException("Unidade informada não foi encontrada")));
                    if (!p.getProduct().getUnit().hasCompatibility(p.getUnit())) {
                        throw new IllegalArgumentException("Unidade informada é inválida para o produto " +
                                p.getProduct().getName() +
                                ". Unidade informada não possui nenhuma conversão para a unidade padrão do item");
                    }
                    p.setPurchaseRequest(request);
                })
                .collect(Collectors.toList());

        request.setRequester(employeeService.getCurrentLoggedEmployee()
                .orElseThrow(() -> new IllegalArgumentException("Solicitante não encontrado")));
        request.setResponsible(responsible);
        request.setCostCenter(this.costCenterService.findById(request.getCostCenter().getId())
                .orElseThrow(() -> new IllegalArgumentException("Centro de custo não encontrado")));
        request.setBranchOffice(this.branchOfficeService.findById(request.getBranchOffice().getId())
                .orElseThrow(() -> new IllegalArgumentException("Filial não encontrada")));
        request.setItems(items);
        request.setStatus(ProcessStatus.PENDING);

        return Optional.of(this.purchaseRequests.save(request));
    }

    @Transactional
    public Optional<PurchaseRequest> update(final Long id, final PurchaseRequest request) {
        return this.findById(id)
                .map(savedRequest -> {
                    if (savedRequest.getStatus() == ProcessStatus.APPROVED || savedRequest.getStatus() == ProcessStatus.REJECTED) {
                        throw new IllegalArgumentException("Não é possível alterar uma solicitação de compra já finalizada. Código " + savedRequest.getId());
                    }

                    if (savedRequest.getItems().stream()
                            .anyMatch(item -> item.isFinalized() && !request.getItems().contains(item))) {
                        throw new IllegalArgumentException("Não é possível remover um item que ja está finalizado");
                    }

                    savedRequest.getItems()
                            .removeIf(item -> !request.getItems().contains(item));

                    request.getItems()
                            .stream()
                            .filter(item -> {
                                final int index = savedRequest.getItems().indexOf(item);
                                return index != -1 &&
                                        !savedRequest.getItems()
                                                .get(index)
                                                .getStatus().equals(ProcessStatus.APPROVED) &&
                                        !savedRequest.getItems()
                                                .get(index)
                                                .getStatus().equals(ProcessStatus.REJECTED);
                            })
                            .forEach(item -> {
                                final int index = savedRequest.getItems().indexOf(item);
                                final PurchaseRequestItem pr = savedRequest.getItems().get(index);
                                pr.setQuantity(item.getQuantity());
                                pr.setProduct(this.products
                                        .findById(item.getProduct().getId())
                                        .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado")));

                                pr.setUnit(unitService.findById(item.getUnit().getId())
                                        .orElseThrow(() -> new IllegalArgumentException("Unidade informada não foi encontrada")));
                                if (!pr.getProduct().getUnit().hasCompatibility(pr.getUnit())) {
                                    throw new IllegalArgumentException("Unidade informada é inválida para o produto " +
                                            pr.getProduct().getName() +
                                            ". Unidade informada não possui nenhuma conversão para a unidade padrão do item");
                                }
                            });

                    savedRequest.getItems()
                            .addAll(
                                    request.getItems()
                                            .stream()
                                            .filter(item -> !savedRequest.getItems().contains(item))
                                            .peek(item -> {
                                                item.setProduct(this.products
                                                        .findById(item.getProduct().getId())
                                                        .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado")));
                                                item.setStatus(ProcessStatus.PENDING);
                                                item.setUnit(unitService.findById(item.getUnit().getId())
                                                        .orElseThrow(() -> new IllegalArgumentException("Unidade informada não foi encontrada")));
                                                if (!item.getProduct().getUnit().hasCompatibility(item.getUnit())) {
                                                    throw new IllegalArgumentException("Unidade informada é inválida para o produto " +
                                                            item.getProduct().getName() +
                                                            ". Unidade informada não possui nenhuma conversão para a unidade padrão do item");
                                                }
                                                item.setPurchaseRequest(savedRequest);
                                            })
                                            .collect(Collectors.toList())
                            );


                    savedRequest.setNote(request.getNote());

                    request.setRequester(employeeService.getCurrentLoggedEmployee()
                            .orElseThrow(() -> new IllegalArgumentException("Solicitante não encontrado")));
                    savedRequest.setResponsible(
                            this.employees.findById(request.getResponsible().getId())
                                    .orElseThrow(() -> new IllegalArgumentException("Responsável não encontrado")));
                    savedRequest.setDateOfNeed(request.getDateOfNeed());

                    savedRequest.setDescription(request.getDescription());
                    savedRequest.setReason(request.getReason());
                    savedRequest.setCostCenter(this.costCenterService.findById(request.getCostCenter().getId())
                            .orElseThrow(() -> new IllegalArgumentException("Centro de custo não encontrado")));
                    savedRequest.setBranchOffice(this.branchOfficeService.findById(request.getBranchOffice().getId())
                            .orElseThrow(() -> new IllegalArgumentException("Filial não encontrada")));


                    return this.purchaseRequests.save(savedRequest);
                });
    }

    public Optional<PurchaseRequest> updatePurchaseRequestStatus(final Long id, ProcessStatus status) {
        return this.findById(id)
                .map(savedRequest -> {
                    savedRequest.setStatus(status);
                    return this.purchaseRequests.save(savedRequest);
                });
    }

    public Page<PurchaseRequestItem> findItemsByName(String name, Integer page, Integer pageSize) {
        return this.purchaseRequestItemsRepository.findByProductNameContainingIgnoreCase(
                name, PageRequest.of(page, pageSize, Sort.by(Sort.Order.desc("createdDate"), Sort.Order.asc("product.name"))));
    }

    public List<PurchaseRequest> findAll() {
        return this.purchaseRequests.findAll(Sort.by(Sort.Direction.DESC, "createdDate"));
    }

    public List<PurchaseRequest> findAllFilteringByCurrentUser() {
        Employee employee = employeeService.getCurrentLoggedEmployee()
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado"));

        return this.purchaseRequests.findAll(Sort.by(Sort.Direction.DESC, "createdDate"))
                .stream()
                .filter(request -> request.getRequester().isCurrentUserLogged() ||
                        employee.hasRole("ROLE_PURCHASE_REQUESTS_READ_ALL") ||
                        employee.isRoot())
                .collect(Collectors.toList());
    }

    public List<PurchaseRequest> findByStatus(ProcessStatus status) {
        return this.purchaseRequests.findByStatus(status);
    }

    public List<PurchaseRequest> findByRequester(Employee requester) {
        return this.purchaseRequests.findByRequester(requester);
    }

    public File getPurchaseRequestReport(Long requestId) throws SQLException, IOException, JRException {
        return pdfReportService.getPurchaseRequestReport(
                this.purchaseRequests.findById(requestId)
                        .orElseThrow(() -> new IllegalArgumentException("Solicitação de compra não encontrada")));
    }

    public Optional<PurchaseRequest> findById(Long id) {
        return this.purchaseRequests.findById(id);
    }

    public Optional<PurchaseRequest> sendPurchaseRequestToApproval(Long id) {
        return this.approvalService.insertFromPurchaseRequest(
                purchaseRequests.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada")))
                .map(approval -> {
                    if (approval.getStatus() == ProcessStatus.PENDING) {
                        approval.getRequest().setStatus(ProcessStatus.REALIZED);
                    }
                    return this.purchaseRequests.save(approval.getRequest());
                });
    }

    public Optional<PurchaseRequest> cancelPurchaseRequestById(final Long id) {
        return this.findById(id)
                .map(savedRequest -> {
                    if (!StatusesUtil.isPending(savedRequest.getItems())) {
                        throw new IllegalArgumentException("Somente solicitações com status PENDENTE podem ser canceladas");
                    }

                    savedRequest.setStatus(ProcessStatus.CANCELED);
                    savedRequest.getItems().forEach(i -> i.setStatus(ProcessStatus.CANCELED));
                    return this.purchaseRequests.save(savedRequest);
                });
    }
}
