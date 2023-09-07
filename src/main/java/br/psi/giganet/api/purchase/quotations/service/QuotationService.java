package br.psi.giganet.api.purchase.quotations.service;

import br.psi.giganet.api.purchase.approvals.model.Approval;
import br.psi.giganet.api.purchase.approvals.model.ApprovalItem;
import br.psi.giganet.api.purchase.approvals.service.ApprovalService;
import br.psi.giganet.api.purchase.branch_offices.service.BranchOfficeService;
import br.psi.giganet.api.purchase.common.emails.services.EmailService;
import br.psi.giganet.api.purchase.common.reports.pdf.services.PdfReportService;
import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.common.utils.statuses.StatusesUtil;
import br.psi.giganet.api.purchase.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.api.purchase.config.security.service.AuthUtilsService;
import br.psi.giganet.api.purchase.cost_center.service.CostCenterService;
import br.psi.giganet.api.purchase.employees.service.EmployeeService;
import br.psi.giganet.api.purchase.locations.service.LocationService;
import br.psi.giganet.api.purchase.payment_conditions.service.PaymentConditionService;
import br.psi.giganet.api.purchase.products.model.Product;
import br.psi.giganet.api.purchase.products.service.ProductService;
import br.psi.giganet.api.purchase.projects.service.ProjectService;
import br.psi.giganet.api.purchase.quotation_approvals.service.QuotationApprovalService;
import br.psi.giganet.api.purchase.quotations.model.Quotation;
import br.psi.giganet.api.purchase.quotations.model.QuotedItem;
import br.psi.giganet.api.purchase.quotations.model.SupplierItemQuotation;
import br.psi.giganet.api.purchase.quotations.model.enums.FreightType;
import br.psi.giganet.api.purchase.quotations.repository.QuotationEagerProjection;
import br.psi.giganet.api.purchase.quotations.repository.QuotationRepository;
import br.psi.giganet.api.purchase.quotations.repository.QuotedItemsRepository;
import br.psi.giganet.api.purchase.quotations.repository.SupplierItemQuotationRepository;
import br.psi.giganet.api.purchase.suppliers.model.Supplier;
import br.psi.giganet.api.purchase.suppliers.service.SupplierService;
import br.psi.giganet.api.purchase.units.service.UnitService;
import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QuotationService {

    @Autowired
    private QuotationRepository quotationRepository;

    @Autowired
    private QuotedItemsRepository quotedItemsRepository;

    @Autowired
    private SupplierItemQuotationRepository supplierQuotedItemsRepository;

    @Autowired
    private SupplierService suppliers;

    @Autowired
    private ProductService products;

    @Autowired
    private ApprovalService approvals;

    @Autowired
    private EmployeeService employees;

    @Autowired
    private QuotationApprovalService quotationApprovals;

    @Autowired
    private UnitService units;

    @Autowired
    private AuthUtilsService authService;

    @Autowired
    private CostCenterService costCenterService;

    @Autowired
    private BranchOfficeService branchOfficeService;

    @Autowired
    private PaymentConditionService paymentConditionService;

    @Autowired
    private PdfReportService pdfReportService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private LocationService locationService;

    public Optional<Quotation> save(Quotation quotation) {
        return Optional.of(this.quotationRepository.save(quotation));
    }

    @Deprecated(forRemoval = true)
    public Optional<Quotation> insertFromApproval(Approval approval) {
        final Quotation quotation = new Quotation();
        quotation.setStatus(ProcessStatus.PENDING);
        quotation.setCostCenter(approval.getRequest().getCostCenter());
        quotation.setItems(
                approval.getItems()
                        .stream()
                        .filter(ApprovalItem::isApproved)
                        .map(ap -> {
                            final QuotedItem item = new QuotedItem();
                            item.setApprovedItem(ap);
                            item.setProduct(ap.getItem().getProduct());
                            item.setQuotation(quotation);
                            item.setStatus(ProcessStatus.PENDING);
                            item.setQuantity(ap.getItem().getQuantity());
                            item.setUnit(ap.getItem().getUnit());
                            return item;
                        })
                        .collect(Collectors.toList())
        );
        return this.save(quotation);
    }

    public Optional<Quotation> insert(Quotation quotation) {
        quotation.setResponsible(employees.getCurrentLoggedEmployee()
                .orElseThrow(() -> new IllegalArgumentException("Responsável não encontrado")));
        quotation.setStatus(ProcessStatus.PENDING);
        quotation.setItems(
                quotation.getItems()
                        .stream()
                        .peek(item -> {
                            if (item.getApprovedItem().getId() != null) {
                                item.setApprovedItem(this.approvals.findItemById(item.getApprovedItem().getId())
                                        .orElseThrow(() -> new IllegalArgumentException("Item de aprovação id "
                                                + item.getApprovedItem().getId() + " não encontrado")));

                                if (!item.getApprovedItem().getItem().getUnit().equals(item.getUnit())) {
                                    throw new IllegalArgumentException("A unidade solicitada é diferente da unidade selecionada para o item");
                                }
                            } else {
                                item.setApprovedItem(null);
                            }
                            item.setProduct(
                                    this.products.findByCode(item.getProduct().getCode())
                                            .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado. Código "
                                                    + item.getProduct().getId())));
                            item.setQuotation(quotation);
                            item.setStatus(ProcessStatus.PENDING);
                            item.setUnit(
                                    units.findById(item.getUnit().getId())
                                            .orElseThrow(() -> new IllegalArgumentException("Unidade não encontrada")));
                            if (!item.getProduct().getUnit().hasCompatibility(item.getUnit())) {
                                throw new IllegalArgumentException("Unidade informada é inválida para o item " +
                                        item.getProduct().getName() +
                                        ". Unidade informada não possui nenhuma conversão para a unidade padrão do item");
                            }

                            if (item.getSuppliers() == null || item.getSuppliers().isEmpty()) {
                                throw new IllegalArgumentException("Deve ser informado pelo menos um fornecedor");
                            }
                            item.getSuppliers()
                                    .forEach(supplier -> {
                                        supplier.setUnit(
                                                units.findById(supplier.getUnit().getId())
                                                        .orElseThrow(() -> new IllegalArgumentException("Unidade não encontrada")));
                                        if (!supplier.getUnit().hasCompatibility(item.getUnit())) {
                                            throw new IllegalArgumentException("Unidade informada é inválida para o fornecedor " + supplier.getSupplier().getName() +
                                                    " referente ao item " +
                                                    item.getProduct().getName() +
                                                    ". Unidade informada não possui nenhuma conversão para a unidade padrão do item");
                                        }
                                        supplier.setQuotedItem(item);
                                        supplier.setSupplier(
                                                suppliers.findById(supplier.getSupplier().getId())
                                                        .orElseThrow(() -> new IllegalArgumentException("Fornecedor não encontrado. Código " + supplier.getSupplier().getId())));

                                        if (supplier.getDiscount() != null && supplier.getTotal().compareTo(supplier.getDiscount()) < 0) {
                                            throw new IllegalArgumentException("Desconto informado é superior ao total informado para o fornecedor " + supplier.getSupplier().getName() +
                                                    " referente ao item " + item.getProduct().getName() + ".");
                                        }
                                    });

                        })
                        .collect(Collectors.toList()));

        quotation.getItems().forEach(item -> {
            if (item.getSuppliers().stream()
                    .noneMatch(s -> s.getIsSelected() != null && s.getIsSelected())) {
                throw new IllegalArgumentException("Nenhum fornecedor foi selecionado para o item "
                        + item.getProduct().getCode());
            }
        });
        freightValidateHandler(quotation);
        quotation.setCostCenter(this.costCenterService.findById(quotation.getCostCenter().getId())
                .orElseThrow(() -> new IllegalArgumentException("Centro de custo não encontrado")));
        quotation.setBranchOffice(this.branchOfficeService.findById(quotation.getBranchOffice().getId())
                .orElseThrow(() -> new IllegalArgumentException("Filial não encontrada")));
        quotation.getPaymentCondition().setCondition(
                paymentConditionService.findById(quotation.getPaymentCondition().getCondition().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Condição de pagamento não encontrada")));

        quotation.setProject(quotation.getProject() == null ? null :
                projectService.findById(quotation.getProject().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Projeto não encontrado")));

        quotation.setLocation(quotation.getLocation() == null ? null :
                locationService.findById(quotation.getLocation().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Localização não encontrada")));

        quotation.setTotal(
                quotation.getItems().stream()
                        .map(quotedItem -> quotedItem.getSelectedSupplier().getTotal())
                        .reduce(BigDecimal::add)
                        .orElseThrow(() -> new IllegalArgumentException("Total não pode ser nulo"))
                        .add(quotation.getFreight().getPrice()));

        return this.save(quotation);
    }

    public Optional<Quotation> updateStatus(final Quotation quotation) {
        return this.findById(quotation.getId())
                .flatMap(saved -> {
                    saved.setStatus(quotation.getStatus());
                    saved.getItems().forEach(i -> i.setStatus(quotation.getStatus()));
                    return this.save(saved);
                });
    }

    public Optional<Quotation> quoteHandle(final Long id, Quotation quotation) {
        return this.findById(id)
                .map(saved -> {
                    if (saved.getStatus() == ProcessStatus.APPROVED || saved.getStatus() == ProcessStatus.REJECTED) {
                        throw new IllegalArgumentException("Não é possível alterar uma cotação de compra já finalizada. Código " + saved.getId());
                    }

                    saved.setResponsible(employees.getCurrentLoggedEmployee()
                            .orElseThrow(() -> new IllegalArgumentException("Responsável não encontrado")));
                    saved.setNote(quotation.getNote());
                    saved.setDateOfNeed(quotation.getDateOfNeed());
                    saved.setDescription(quotation.getDescription());
                    saved.setExternalLink(quotation.getExternalLink());

                    saved.getItems().removeIf(item -> !quotation.getItems().contains(item));

                    quotation.getItems()
                            .stream()
                            .filter(item -> saved.getItems().contains(item))
                            .forEach(item -> {
                                final int index = saved.getItems().indexOf(item);
                                QuotedItem savedItem = saved.getItems().get(index);

                                if (savedItem.getApprovedItem() == null) {
                                    savedItem.setProduct(this.products.findByCode(item.getProduct().getCode())
                                            .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado. Código " + item.getProduct().getCode())));

                                } else if (!savedItem.getApprovedItem().getItem().getUnit().equals(item.getUnit())) {
                                    throw new IllegalArgumentException("A unidade solicitada é diferente da unidade selecionada para o item");
                                }

                                savedItem.setStatus(ProcessStatus.PENDING);
                                savedItem.setQuantity(item.getQuantity());
                                savedItem.setUnit(
                                        units.findById(item.getUnit().getId())
                                                .orElseThrow(() -> new IllegalArgumentException("Unidade não encontrada")));
                                if (!savedItem.getProduct().getUnit().hasCompatibility(savedItem.getUnit())) {
                                    throw new IllegalArgumentException("Unidade informada é inválida para o item " +
                                            savedItem.getProduct().getName() +
                                            ". Unidade informada não possui nenhuma conversão para a unidade padrão do item");
                                }

                                if (savedItem.getSuppliers() == null) {
                                    savedItem.setSuppliers(new ArrayList<>());
                                }

                                savedItem.getSuppliers()
                                        .removeIf(savedSupplier -> !item.getSuppliers().contains(savedSupplier));

                                item.getSuppliers().stream()
                                        .filter(supplier -> savedItem.getSuppliers().contains(supplier))
                                        .forEach(supplier -> {
                                            final int indexSupplier = savedItem.getSuppliers().indexOf(supplier);
                                            SupplierItemQuotation savedSupplier = savedItem.getSuppliers().get(indexSupplier);

                                            savedSupplier.setSupplier(
                                                    suppliers.findById(supplier.getSupplier().getId())
                                                            .orElseThrow(() -> new IllegalArgumentException("Fornecedor não encontrado. Código " + supplier.getSupplier().getId()))
                                            );
                                            savedSupplier.setIsSelected(supplier.getIsSelected());
                                            savedSupplier.setQuantity(supplier.getQuantity());
                                            savedSupplier.setUnit(
                                                    units.findById(supplier.getUnit().getId())
                                                            .orElseThrow(() -> new IllegalArgumentException("Unidade não encontrada")));
                                            if (!savedSupplier.getUnit().hasCompatibility(savedItem.getUnit())) {
                                                throw new IllegalArgumentException("Unidade informada é inválida para o fornecedor " + supplier.getSupplier().getName() +
                                                        " referente ao item " +
                                                        savedItem.getProduct().getName() +
                                                        ". Unidade informada não possui nenhuma conversão para a unidade padrão do item");
                                            }
                                            savedSupplier.setIcms(supplier.getIcms());
                                            savedSupplier.setIpi(supplier.getIpi());
                                            savedSupplier.setPrice(supplier.getPrice());
                                            savedSupplier.setDiscount(supplier.getDiscount());
                                            savedSupplier.setTotal(supplier.getTotal());

                                            if (savedSupplier.getDiscount() != null && savedSupplier.getTotal().compareTo(savedSupplier.getDiscount()) < 0) {
                                                throw new IllegalArgumentException("Desconto informado é superior ao total informado para o fornecedor " + savedSupplier.getSupplier().getName() +
                                                        " referente ao item " + savedItem.getProduct().getName() + ".");
                                            }
                                        });

                                item.getSuppliers().stream()
                                        .filter(supplier -> !savedItem.getSuppliers().contains(supplier))
                                        .peek(supplier -> {
                                            supplier.setUnit(
                                                    units.findById(supplier.getUnit().getId())
                                                            .orElseThrow(() -> new IllegalArgumentException("Unidade não encontrada")));
                                            if (!supplier.getUnit().hasCompatibility(savedItem.getUnit())) {
                                                throw new IllegalArgumentException("Unidade informada é inválida para o fornecedor " + supplier.getSupplier().getName() +
                                                        " referente ao item " +
                                                        savedItem.getProduct().getName() +
                                                        ". Unidade informada não possui nenhuma conversão para a unidade padrão do item");
                                            }
                                            supplier.setSupplier(
                                                    suppliers.findById(supplier.getSupplier().getId())
                                                            .orElseThrow(() -> new IllegalArgumentException("Fornecedor não encontrado. Código "
                                                                    + supplier.getSupplier().getId()))
                                            );
                                            supplier.setQuotedItem(savedItem);
                                            if (supplier.getDiscount() != null && supplier.getTotal().compareTo(supplier.getDiscount()) < 0) {
                                                throw new IllegalArgumentException("Desconto informado é superior ao total informado para o fornecedor " + supplier.getSupplier().getName() +
                                                        " referente ao item " + savedItem.getProduct().getName() + ".");
                                            }
                                            savedItem.getSuppliers().add(supplier);
                                        })
                                        .collect(Collectors.toList())
                                        .forEach(supplier -> savedItem.getSuppliers().add(supplier));
                            });

                    quotation.getItems()
                            .stream()
                            .filter(item -> !saved.getItems().contains(item))
                            .peek(item -> {
                                item.setQuotation(saved);
                                item.setStatus(ProcessStatus.PENDING);
                                if (item.getApprovedItem().getId() != null) {
                                    item.setApprovedItem(this.approvals.findItemById(item.getApprovedItem().getId())
                                            .orElseThrow(() -> new IllegalArgumentException("Item de aprovação id " + item.getApprovedItem().getId() + " não encontrado")));

                                    if (!item.getApprovedItem().getItem().getUnit().equals(item.getUnit())) {
                                        throw new IllegalArgumentException("A unidade solicitada é diferente da unidade selecionada para o item");
                                    }

                                } else {
                                    item.setApprovedItem(null);
                                }
                                item.setProduct(this.products.findByCode(item.getProduct().getCode())
                                        .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado. Código " + item.getProduct().getCode())));
                                item.setUnit(
                                        units.findById(item.getUnit().getId())
                                                .orElseThrow(() -> new IllegalArgumentException("Unidade não encontrada")));
                                if (!item.getProduct().getUnit().hasCompatibility(item.getUnit())) {
                                    throw new IllegalArgumentException("Unidade informada é inválida para o item " +
                                            item.getProduct().getName() +
                                            ". Unidade informada não possui nenhuma conversão para a unidade padrão do item");
                                }

                                if (item.getSuppliers() == null || item.getSuppliers().isEmpty()) {
                                    throw new IllegalArgumentException("Deve ser informado pelo menos um fornecedor");
                                }
                                item.getSuppliers()
                                        .forEach(supplier -> {
                                            supplier.setUnit(
                                                    units.findById(supplier.getUnit().getId())
                                                            .orElseThrow(() -> new IllegalArgumentException("Unidade não encontrada")));
                                            if (!supplier.getUnit().hasCompatibility(item.getUnit())) {
                                                throw new IllegalArgumentException("Unidade informada é inválida para o fornecedor " + supplier.getSupplier().getName() +
                                                        " referente ao item " +
                                                        item.getProduct().getName() +
                                                        ". Unidade informada não possui nenhuma conversão para a unidade padrão do item");
                                            }
                                            supplier.setQuotedItem(item);
                                            supplier.setSupplier(
                                                    suppliers.findById(supplier.getSupplier().getId())
                                                            .orElseThrow(() -> new IllegalArgumentException("Fornecedor não encontrado. Código " + supplier.getSupplier().getId())));

                                            if (supplier.getDiscount() != null && supplier.getTotal().compareTo(supplier.getDiscount()) < 0) {
                                                throw new IllegalArgumentException("Desconto informado é superior ao total informado para o fornecedor " + supplier.getSupplier().getName() +
                                                        " referente ao item " + item.getProduct().getName() + ".");
                                            }
                                        });
                            })
                            .collect(Collectors.toList())
                            .forEach(item -> saved.getItems().add(item));

                    saved.getItems().forEach(item -> {
                        if (item.getSuppliers().stream()
                                .noneMatch(s -> s.getIsSelected() != null && s.getIsSelected())) {
                            throw new IllegalArgumentException("Nenhum fornecedor foi selecionado para o item "
                                    + item.getProduct().getCode());
                        }
                    });
                    freightValidateHandler(quotation);
                    saved.setFreight(quotation.getFreight());
                    saved.getPaymentCondition().setCondition(
                            paymentConditionService.findById(quotation.getPaymentCondition().getCondition().getId())
                                    .orElseThrow(() -> new IllegalArgumentException("Condição de pagamento não encontrada")));

                    saved.getPaymentCondition()
                            .getDueDates()
                            .removeIf(date -> !quotation.getPaymentCondition().getDueDates().contains(date));

                    quotation.getPaymentCondition()
                            .getDueDates()
                            .stream()
                            .filter(date -> saved.getPaymentCondition().getDueDates().contains(date))
                            .collect(Collectors.toList())
                            .forEach(date -> {
                                final int index = saved.getPaymentCondition().getDueDates().indexOf(date);
                                saved.getPaymentCondition().getDueDates()
                                        .get(index)
                                        .setDueDate(date.getDueDate());
                            });
                    saved.getPaymentCondition().getDueDates()
                            .addAll(quotation.getPaymentCondition()
                                    .getDueDates()
                                    .stream()
                                    .filter(date -> !saved.getPaymentCondition().getDueDates().contains(date))
                                    .peek(date -> date.setCondition(saved.getPaymentCondition()))
                                    .collect(Collectors.toList()));

                    saved.setCostCenter(this.costCenterService.findById(quotation.getCostCenter().getId())
                            .orElseThrow(() -> new IllegalArgumentException("Centro de custo não encontrado")));
                    saved.setBranchOffice(this.branchOfficeService.findById(quotation.getBranchOffice().getId())
                            .orElseThrow(() -> new IllegalArgumentException("Filial não encontrada")));

                    saved.setProject(quotation.getProject() == null ? null :
                            projectService.findById(quotation.getProject().getId())
                                    .orElseThrow(() -> new IllegalArgumentException("Projeto não encontrado")));

                    saved.setLocation(quotation.getLocation() == null ? null :
                            locationService.findById(quotation.getLocation().getId())
                                    .orElseThrow(() -> new IllegalArgumentException("Localização não encontrada")));

                    saved.setTotal(
                            saved.getItems().stream()
                                    .map(quotedItem -> quotedItem.getSelectedSupplier().getTotal())
                                    .reduce(BigDecimal::add)
                                    .orElseThrow(() -> new IllegalArgumentException("Total não pode ser nulo"))
                                    .add(saved.getFreight().getPrice()));

                    saved.setStatus(ProcessStatus.PENDING);
                    return this.quotationRepository.save(saved);
                });
    }


    public Optional<Quotation> markQuotationAsFinalized(Long id) {
        return this.findById(id)
                .map(quotation -> {
                    if (!StatusesUtil.isPending(quotation.getItems())) {
                        throw new IllegalArgumentException("Somente cotações pendentes podem ser marcadas como finalizadas");
                    }
                    quotation.setStatus(ProcessStatus.REALIZED);
                    quotation.getItems().forEach(item -> item.setStatus(ProcessStatus.REALIZED));

                    quotation.setApproval(this.quotationApprovals.insertFromQuotation(quotation)
                            .orElseThrow(() -> new IllegalArgumentException("Não foi possível criar uma aprovação para esta aprovação")));

                    return this.quotationRepository.save(quotation);
                });
    }

    public List<Quotation> findAll() {
        return this.quotationRepository.findAll(Sort.by(Sort.Direction.DESC, "createdDate"));
    }

    public List<QuotationEagerProjection> findAllWithFetchEager() {
        return this.quotationRepository.findAllWithFetchEager(Sort.by(Sort.Direction.DESC, "createdDate"));
    }

    public Optional<Quotation> findById(Long id) {
        return this.quotationRepository.findById(id);
    }

    public Optional<SupplierItemQuotation> findLastBySupplierAndProduct(Supplier supplier, Product product) {
        return this.supplierQuotedItemsRepository.findLastBySupplierAndProduct(
                supplier,
                product,
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "lastModifiedDate")))
                .stream()
                .findFirst();
    }

    public Optional<SupplierItemQuotation> findLastBySupplierAndProduct(Supplier supplier, String product) {
        return this.supplierQuotedItemsRepository.findLastBySupplierAndProduct(
                supplier,
                product,
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "lastModifiedDate")))
                .stream()
                .findFirst();
    }

    public Page<QuotedItem> findItemsByName(String name, Integer page, Integer pageSize) {
        return this.quotedItemsRepository.findByProductNameContainingIgnoreCase(
                name, PageRequest.of(page, pageSize, Sort.by(Sort.Order.desc("createdDate"), Sort.Order.asc("product.name"))));
    }

    public File getQuotationReport(Long quotationId, Long supplierId) throws SQLException, IOException, JRException {
        return this.pdfReportService.getQuotationReport(
                this.quotationRepository.findById(quotationId)
                        .orElseThrow(() -> new IllegalArgumentException("Cotação não encontrada")),
                this.suppliers.findById(supplierId)
                        .orElseThrow(() -> new IllegalArgumentException("Fornecedor não encontrado"))
        );
    }

    public File getQuotationReport(Long quotationId) throws SQLException, IOException, JRException {
        return this.pdfReportService.getQuotationReport(
                this.quotationRepository.findById(quotationId)
                        .orElseThrow(() -> new IllegalArgumentException("Cotação não encontrada"))
        );
    }

    @Transactional
    public void sendEmailWithQuotation(Long quotationId, Supplier supplier, String subject, String message) throws SQLException, IOException, JRException, MessagingException {
        Supplier foundSupplier = this.suppliers.findById(supplier.getId())
                .orElseThrow(() -> new IllegalArgumentException("Fornecedor não encontrado"));

        emailService.sendEmail(
                subject,
                message,
                getQuotationReport(quotationId, foundSupplier.getId()),
                this.authService.getCurrentUsername()
                        .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado")),
                supplier.getEmail()
        );
    }

    public Optional<Quotation> cancelQuotationById(final Long id) {
        return this.findById(id)
                .map(saved -> {
                    final boolean isPending = StatusesUtil.isPending(saved.getItems());
                    final boolean isRealized = StatusesUtil.isRealized(saved.getItems());
                    if (!isPending && !isRealized) {
                        throw new IllegalArgumentException("Somente cotações com status PENDENTE ou REALIZADA podem ser canceladas");
                    }

                    if (isRealized) {
                        quotationApprovals.cancelApprovalByQuotation(saved)
                                .orElseThrow(() -> new IllegalArgumentException(
                                        "Não foi possível cancelar a cotação. Aprovação de cotação não foi encontrada"));
                    }

                    saved.setStatus(ProcessStatus.CANCELED);
                    saved.getItems().forEach(i -> i.setStatus(ProcessStatus.CANCELED));
                    return this.quotationRepository.save(saved);
                });
    }

    private void freightValidateHandler(final Quotation quotation) {
        if (quotation.getFreight().getType().equals(FreightType.FOB) &&
                (quotation.getFreight().getPrice() == null ||
                        quotation.getFreight().getPrice().compareTo(BigDecimal.ZERO) < 0)) {
            throw new IllegalArgumentException("O valor do frete é inválido");
        } else if (quotation.getFreight().getType().equals(FreightType.CIF)) {
            quotation.getFreight().setPrice(BigDecimal.ZERO);
        }
    }
}
