package br.psi.giganet.api.purchase.purchase_order.service;

import br.psi.giganet.api.purchase.branch_offices.model.BranchOffice;
import br.psi.giganet.api.purchase.branch_offices.service.BranchOfficeService;
import br.psi.giganet.api.purchase.common.notifications.service.NotificationService;
import br.psi.giganet.api.purchase.common.reports.pdf.services.PdfReportService;
import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.common.webhooks.services.WebhooksHandlerService;
import br.psi.giganet.api.purchase.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.api.purchase.cost_center.model.CostCenter;
import br.psi.giganet.api.purchase.cost_center.service.CostCenterService;
import br.psi.giganet.api.purchase.delivery_addresses.service.DeliveryAddressesService;
import br.psi.giganet.api.purchase.employees.model.Employee;
import br.psi.giganet.api.purchase.employees.service.EmployeeService;
import br.psi.giganet.api.purchase.payment_conditions.service.PaymentConditionService;
import br.psi.giganet.api.purchase.products.model.Product;
import br.psi.giganet.api.purchase.products.service.ProductService;
import br.psi.giganet.api.purchase.purchase_order.adapter.PurchaseOrderAdapter;
import br.psi.giganet.api.purchase.purchase_order.model.*;
import br.psi.giganet.api.purchase.purchase_order.repository.AdvancedPurchaseOrderRepository;
import br.psi.giganet.api.purchase.purchase_order.repository.PurchaseOrderItemRepository;
import br.psi.giganet.api.purchase.purchase_order.repository.PurchaseOrderRepository;
import br.psi.giganet.api.purchase.purchase_order.repository.dto.AdvancedPurchaseOrderDTO;
import br.psi.giganet.api.purchase.purchase_order.repository.dto.OrderItemDTO;
import br.psi.giganet.api.purchase.purchase_order.repository.dto.OrderWithQuotationAndCompetenciesDTO;
import br.psi.giganet.api.purchase.purchase_order.repository.dto.PurchaseOrderWithQuotationDTO;
import br.psi.giganet.api.purchase.quotation_approvals.model.QuotationApproval;
import br.psi.giganet.api.purchase.quotation_approvals.service.QuotationApprovalService;
import br.psi.giganet.api.purchase.quotations.model.SupplierItemQuotation;
import br.psi.giganet.api.purchase.quotations.model.enums.FreightType;
import br.psi.giganet.api.purchase.suppliers.model.Supplier;
import br.psi.giganet.api.purchase.suppliers.service.SupplierService;
import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PurchaseOrderService {

    @Autowired
    private PurchaseOrderRepository ordersRepository;

    @Autowired
    private AdvancedPurchaseOrderRepository advancedPurchaseOrderRepository;

    @Autowired
    private PurchaseOrderItemRepository orderItemRepository;

    @Autowired
    private CostCenterService costCenterService;

    @Autowired
    private PdfReportService pdfReportService;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private DeliveryAddressesService addressService;

    @Autowired
    private PaymentConditionService paymentConditionService;

    @Autowired
    private WebhooksHandlerService webhooksHandlerService;
    @Autowired
    private QuotationApprovalService quotationApprovalService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private ProductService productService;

    @Autowired
    private PurchaseOrderAdapter adapter;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private BranchOfficeService branchOfficeService;

    public List<PurchaseOrder> findAll() {
        return this.ordersRepository.findAll(Sort.by(Sort.Direction.DESC, "createdDate"));
    }

    public List<PurchaseOrderWithQuotationDTO> findAllWithQuotation() {
        return advancedPurchaseOrderRepository.findAllWithQuotation(Sort.by(Sort.Direction.DESC, "createdDate"));
    }

    public List<OrderWithQuotationAndCompetenciesDTO> findAllWithQuotationAndCompetencies() {
        return advancedPurchaseOrderRepository.findAllWithQuotationAndCompetencies();
    }

    public Page<AdvancedPurchaseOrderDTO> findAllAdvanced(List<String> queries, Integer page, Integer pageSize) {
        return advancedPurchaseOrderRepository.findAllByAdvancedSearch(
                queries,
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate")));
    }

    public Optional<PurchaseOrder> findById(final Long id) {
        return this.ordersRepository.findById(id);
    }

    public void insertFromQuotationApproval(final QuotationApproval approval) {
        if (ordersRepository.findByApproval(approval).isEmpty()) {

            Set<Supplier> selectedSuppliers = approval.getQuotation()
                    .getItems()
                    .stream()
                    .map(item -> {
                        SupplierItemQuotation supplierItemQuotation = item.getSelectedSupplier();
                        if (supplierItemQuotation == null) {
                            throw new IllegalArgumentException("Fornecedor não selecionado");
                        }
                        return supplierItemQuotation.getSupplier();
                    })
                    .collect(Collectors.toSet());

            selectedSuppliers.forEach(supplier -> {
                final PurchaseOrder order = new PurchaseOrder();
                order.setApproval(approval);
                order.setSupplier(supplier);
                order.setStatus(ProcessStatus.PENDING);
                order.setResponsible(approval.getQuotation().getResponsible());
                order.setCostCenter(approval.getQuotation().getCostCenter());
                order.setBranchOffice(approval.getQuotation().getBranchOffice());
                order.setDateOfNeed(approval.getQuotation().getDateOfNeed());
                order.setProject(approval.getQuotation().getProject());
                order.setLocation(approval.getQuotation().getLocation());
                order.setItems(
                        approval.getQuotation()
                                .getItems()
                                .stream()
                                .filter(item -> supplier.equals(item.getSelectedSupplier().getSupplier()))
                                .map(quotedItem -> {
                                    final PurchaseOrderItem item = new PurchaseOrderItem();
                                    item.setOrder(order);
                                    item.setProduct(quotedItem.getProduct());
                                    item.setQuantity(quotedItem.getQuantity());
                                    item.setUnit(quotedItem.getUnit());

                                    SupplierItemQuotation supplierItemQuotation = quotedItem.getSelectedSupplier();

                                    item.setPrice(supplierItemQuotation.getPrice());
                                    item.setIpi(supplierItemQuotation.getIpi());
                                    item.setIcms(supplierItemQuotation.getIcms());
                                    item.setDiscount(supplierItemQuotation.getDiscount());
                                    item.setTotal(supplierItemQuotation.getTotal());
                                    item.setSupplier(supplierItemQuotation.getSupplier());
                                    item.setStatus(ProcessStatus.PENDING);

                                    return item;
                                })
                                .collect(Collectors.toList())
                );

                order.setPaymentCondition(new OrderPaymentCondition());
                order.getPaymentCondition().setCondition(
                        approval.getQuotation().getPaymentCondition().getCondition());
                order.getPaymentCondition().setOrder(order);
                order.getPaymentCondition().setDueDates(
                        approval.getQuotation().getPaymentCondition()
                                .getDueDates()
                                .stream()
                                .map(d -> {
                                    OrderConditionDueDate date = new OrderConditionDueDate();
                                    date.setCondition(order.getPaymentCondition());
                                    date.setDueDate(d.getDueDate());
                                    return date;
                                })
                                .collect(Collectors.toList())
                );

                order.setFreight(new PurchaseOrderFreight());
                order.getFreight().setOrder(order);
                order.getFreight().setDeliveryAddress(addressService.getDeliveryAddressDefault().getAddress());
                order.getFreight().setPrice( // freight is divided equally between suppliers
                        approval.getQuotation().getFreight()
                                .getPrice()
                                .divide(BigDecimal.valueOf(selectedSuppliers.size()), RoundingMode.CEILING));
                order.getFreight().setType(approval.getQuotation().getFreight().getType());
                order.getFreight().setDeliveryDate(
                        approval.getQuotation().getDateOfNeed() == null ? ZonedDateTime.now() :
                                ZonedDateTime.of(
                                        approval.getQuotation().getDateOfNeed().atStartOfDay(),
                                        ZoneId.of("UTC")));

                order.setNote(approval.getQuotation().getNote());
                order.setExternalLink(approval.getQuotation().getExternalLink());

                order.setTotal(
                        order.getItems().stream()
                                .map(PurchaseOrderItem::getTotal)
                                .reduce(BigDecimal::add)
                                .orElseThrow(() -> new IllegalArgumentException("Total não pode ser nulo"))
                                .add(order.getFreight().getPrice())
                );

                order.setCompetencies(new ArrayList<>());

                save(order);
            });
        }
    }

    public Optional<PurchaseOrder> update(Long id, PurchaseOrder order) {
        return this.findById(id)
                .map(saved -> {
                    Employee employee = this.employeeService.getCurrentLoggedEmployee()
                            .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado"));
                    if (!employee.isRoot() && !employee.hasRole("ROLE_PURCHASE_ORDERS_WRITE_ROOT")) {
                        throw new AccessDeniedException("Funcionário não possui permissão suficiente para realizar esta operação");
                    }

                    final List<ProcessStatus> validStatuses = Arrays.asList(
                            ProcessStatus.PENDING, ProcessStatus.REALIZED, ProcessStatus.IN_TRANSIT);
                    if (!validStatuses.contains(saved.getStatus())) {
                        throw new IllegalArgumentException("Esta ordem de compra não pode mais ser editada devido ao sua situação atual");
                    } else if (!validStatuses.contains(order.getStatus()) && !order.getStatus().equals(ProcessStatus.CANCELED)) {
                        throw new IllegalArgumentException("O status informado para esta ordem de compra é inválido. " +
                                "São permitidos apenas PENDENTE, EM TRANSITO, REALIZADA ou CANCELADA para este estágio");
                    }
                    saved.setStatus(order.getStatus());

                    if (order.getStatus().equals(ProcessStatus.CANCELED)) {
                        saved.getItems().forEach(i -> i.setStatus(ProcessStatus.CANCELED));
                    }

                    saved.setBranchOffice(branchOfficeService.findById(order.getBranchOffice().getId())
                            .orElseThrow(() -> new IllegalArgumentException("Filial não encontrada")));

                    saved.setNote(order.getNote());
                    freightValidateHandler(order);
                    saved.setTotal(saved.getTotal()
                            .subtract(saved.getFreight().getPrice())
                            .add(order.getFreight().getPrice()));

                    saved.getFreight().setDeliveryDate(order.getFreight().getDeliveryDate());
                    saved.getFreight().setDeliveryAddress(order.getFreight().getDeliveryAddress());
                    saved.getFreight().setType(order.getFreight().getType());
                    saved.getFreight().setPrice(order.getFreight().getPrice());

                    saved.getPaymentCondition().setCondition(
                            paymentConditionService.findById(order.getPaymentCondition().getCondition().getId())
                                    .orElseThrow(() -> new IllegalArgumentException("Condição de pagamento não encontrada")));

                    saved.getPaymentCondition()
                            .getDueDates()
                            .removeIf(date -> !order.getPaymentCondition().getDueDates().contains(date));

                    order.getPaymentCondition()
                            .getDueDates()
                            .stream()
                            .filter(date -> saved.getPaymentCondition().getDueDates().contains(date))
                            .forEach(date -> {
                                int index = saved.getPaymentCondition().getDueDates().indexOf(date);
                                saved.getPaymentCondition()
                                        .getDueDates()
                                        .get(index)
                                        .setDueDate(date.getDueDate());
                            });

                    saved.getPaymentCondition().getDueDates()
                            .addAll(order.getPaymentCondition()
                                    .getDueDates()
                                    .stream()
                                    .filter(date -> !saved.getPaymentCondition().getDueDates().contains(date))
                                    .peek(date -> date.setCondition(order.getPaymentCondition()))
                                    .collect(Collectors.toList()));

                    if (saved.getCompetencies() == null) {
                        saved.setCompetencies(new ArrayList<>());

                    } else {
                        saved.getCompetencies()
                                .removeIf(competence -> !order.getCompetencies().contains(competence));

                        saved.getCompetencies()
                                .stream()
                                .filter(competence -> order.getCompetencies().contains(competence))
                                .forEach(competence -> {
                                    int index = order.getCompetencies().indexOf(competence);
                                    PurchaseOrderCompetence found = order.getCompetencies().get(index);
                                    competence.setDate(found.getDate());
                                    competence.setTotal(found.getTotal());
                                    competence.setCostCenter(costCenterService
                                            .findById(found.getCostCenter().getId())
                                            .orElseThrow(() -> new IllegalArgumentException("Centro de custo não encontrado")));
                                });

                    }

                    saved.getCompetencies().addAll(
                            order.getCompetencies()
                                    .stream()
                                    .peek(competence -> {
                                        competence.setCostCenter(costCenterService
                                                .findById(competence.getCostCenter().getId())
                                                .orElseThrow(() -> new IllegalArgumentException("Centro de custo não encontrado")));
                                        competence.setOrder(saved);
                                    })
                                    .collect(Collectors.toList()));

                    return save(saved);
                });
    }

    public Optional<PurchaseOrder> updatePurchaseOrderCompetencies(Long id, PurchaseOrder order) {
        return ordersRepository.findById(id)
                .map(saved -> {
                    Employee employee = this.employeeService.getCurrentLoggedEmployee()
                            .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado"));
                    if (!employee.isRoot() && !employee.hasRole("ROLE_PURCHASE_ORDERS_COMPETENCIES_WRITE")) {
                        throw new AccessDeniedException("Funcionário não possui permissão suficiente para realizar esta operação");
                    }

                    final List<ProcessStatus> validStatuses = Arrays.asList(
                            ProcessStatus.PARTIALLY_RECEIVED, ProcessStatus.RECEIVED, ProcessStatus.FINALIZED);
                    if (saved.getStatus().equals(ProcessStatus.FINALIZED) || saved.getStatus().equals(ProcessStatus.CANCELED)) {
                        throw new IllegalArgumentException("Esta ordem de compra não pode mais ser editada devido ao sua situação atual");
                    } else if (!validStatuses.contains(order.getStatus())) {
                        throw new IllegalArgumentException("O status informado para esta ordem de compra é inválido");
                    }

                    if (order.getStatus().equals(ProcessStatus.FINALIZED)) {
                        saved.getItems().forEach(i -> i.setStatus(ProcessStatus.RECEIVED));
                    }
                    saved.setStatus(order.getStatus());

                    saved.setNote(order.getNote());

                    if (saved.getCompetencies() == null) {
                        saved.setCompetencies(new ArrayList<>());

                    } else {
                        saved.getCompetencies()
                                .removeIf(competence -> !order.getCompetencies().contains(competence));

                        saved.getCompetencies()
                                .stream()
                                .filter(competence -> order.getCompetencies().contains(competence))
                                .forEach(competence -> {
                                    int index = order.getCompetencies().indexOf(competence);
                                    PurchaseOrderCompetence found = order.getCompetencies().get(index);
                                    if (found.getFiscalDocument() == null) {
                                        throw new IllegalArgumentException("Nota fiscal não pode ser nula");
                                    }
                                    competence.setFiscalDocument(found.getFiscalDocument());

                                    competence.setDate(found.getDate());
                                    competence.setTotal(found.getTotal());
                                    competence.setCostCenter(costCenterService
                                            .findById(found.getCostCenter().getId())
                                            .orElseThrow(() -> new IllegalArgumentException("Centro de custo não encontrado")));
                                });

                    }

                    saved.getCompetencies().addAll(
                            order.getCompetencies()
                                    .stream()
                                    .peek(competence -> {
                                        competence.setCostCenter(costCenterService
                                                .findById(competence.getCostCenter().getId())
                                                .orElseThrow(() -> new IllegalArgumentException("Centro de custo não encontrado")));
                                        competence.setOrder(saved);
                                    })
                                    .collect(Collectors.toList()));

                    return save(saved);
                });
    }

    public Optional<PurchaseOrder> updateStatusByEntry(PurchaseOrder order) {
        return this.findById(order.getId())
                .map(saved -> {
                    if (saved.getStatus().equals(ProcessStatus.CANCELED) ||
                            saved.getStatus().equals(ProcessStatus.FINALIZED) ||
                            saved.getStatus().equals(ProcessStatus.RECEIVED)) {
                        throw new IllegalArgumentException("Ordem já finalizada");
                    }

                    saved.setStatus(order.getStatus());
                    saved.getItems().forEach(savedItem -> {
                        final int index = order.getItems().indexOf(savedItem);
                        savedItem.setStatus(order.getItems().get(index).getStatus());
                    });

                    final PurchaseOrder updated = this.ordersRepository.save(saved);
                    notificationService.onUpdatePurchaseOrderByEntry(updated);
                    return updated;
                });
    }

    public Set<PurchaseOrderItem> findAllLastPurchaseByQuotationApproval(Long approvalId) {
        Optional<QuotationApproval> approval = this.quotationApprovalService.findById(approvalId);
        if (approval.isEmpty()) {
            throw new IllegalArgumentException("Aprovação de cotação não encontrada");
        }

        return approval.get().getQuotation().getItems()
                .stream()
                .map(quotedItem -> this.findLastByProduct(quotedItem.getProduct()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public Set<PurchaseOrderItem> findAllLastPurchaseByProducts(List<Product> products) {
        return products
                .stream()
                .map(product -> this.findLastByProduct(
                        this.productService.findById(product.getId())
                                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado")))
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public Optional<PurchaseOrderItem> findLastByProduct(Product product) {
        return this.orderItemRepository.findLastByProduct(
                product,
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "lastModifiedDate")))
                .stream()
                .findFirst();
    }

    public Page<OrderItemDTO> findAllByStatusNotCanceled(Integer page, Integer pageSize) {
        return this.orderItemRepository.findAllByStatusNotCanceled(
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "lastModifiedDate")));
    }

    public File getPurchaseOrderReport(Long orderId) throws SQLException, IOException, JRException {
        return pdfReportService.getPurchaseOrderReport(
                this.ordersRepository.findById(orderId)
                        .orElseThrow(() -> new IllegalArgumentException("Ordem de compra não encontrada")));
    }

    public File getPurchaseOrdersListByCompetenceReport(
            LocalDate competence,
            List<BranchOffice> branchOffices,
            List<ProcessStatus> statuses) throws SQLException, IOException, JRException {
        return pdfReportService.getPurchaseOrdersListByCompetenceReport(
                competence,
                branchOffices,
                statuses);
    }

    public File getPurchaseOrdersListByCompetenceAndCostCenterReport(
            LocalDate competence,
            List<CostCenter> costCenters,
            List<ProcessStatus> statuses) throws SQLException, IOException, JRException {
        return pdfReportService.getPurchaseOrdersListByCompetenceAndCostCenterReport(
                competence,
                costCenters,
                statuses);
    }

    public File getPurchaseOrdersListByCompetenceAndCostCenterAndBranchOfficeReport(
            LocalDate competence,
            List<CostCenter> costCenters,
            List<ProcessStatus> statuses,
            BranchOffice branchOffice) throws SQLException, IOException, JRException {
        return pdfReportService.getPurchaseOrdersListByCompetenceAndCostCenterAndBranchOfficeReport(
                competence,
                costCenters,
                statuses,
                branchOfficeService.findById(branchOffice.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Filial não encontrada")));
    }

    public File getPurchaseOrdersBySupplierReport(
            Supplier supplier,
            LocalDate initialDate,
            LocalDate finalDate,
            List<ProcessStatus> statuses) throws SQLException, IOException, JRException {
        return pdfReportService.getPurchaseOrdersBySupplierReport(supplier, initialDate, finalDate, statuses);
    }

    public Optional<PurchaseOrderItem> findLastByProduct(String product) {
        return this.orderItemRepository.findLastByProduct(
                product,
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "lastModifiedDate")))
                .stream()
                .findFirst();
    }

    private void freightValidateHandler(final PurchaseOrder order) {
        if (order.getFreight().getType().equals(FreightType.FOB) &&
                (order.getFreight().getPrice() == null ||
                        order.getFreight().getPrice().compareTo(BigDecimal.ZERO) <= 0)) {
            throw new IllegalArgumentException("O valor do frete é inválido");
        } else if (order.getFreight().getType().equals(FreightType.CIF)) {
            order.getFreight().setPrice(BigDecimal.ZERO);
        }
    }

    private PurchaseOrder save(PurchaseOrder order) {
        final PurchaseOrder saved = ordersRepository.save(order);
        webhooksHandlerService.onSavePurchaseOrder(order);
        return saved;
    }

}
