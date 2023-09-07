package br.psi.giganet.api.purchase.integration.utils;

import br.psi.giganet.api.purchase.approvals.model.Approval;
import br.psi.giganet.api.purchase.approvals.model.ApprovalItem;
import br.psi.giganet.api.purchase.approvals.repository.ApprovalRepository;
import br.psi.giganet.api.purchase.branch_offices.model.BranchOffice;
import br.psi.giganet.api.purchase.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.api.purchase.common.address.model.Address;
import br.psi.giganet.api.purchase.common.notifications.model.Notification;
import br.psi.giganet.api.purchase.common.notifications.model.NotificationEmployee;
import br.psi.giganet.api.purchase.common.notifications.model.NotificationRole;
import br.psi.giganet.api.purchase.common.notifications.model.NotificationType;
import br.psi.giganet.api.purchase.common.notifications.repository.NotificationRepository;
import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.config.security.model.Permission;
import br.psi.giganet.api.purchase.config.security.repository.PermissionRepository;
import br.psi.giganet.api.purchase.cost_center.model.CostCenter;
import br.psi.giganet.api.purchase.cost_center.repository.CostCenterRepository;
import br.psi.giganet.api.purchase.delivery_addresses.model.DeliveryAddress;
import br.psi.giganet.api.purchase.delivery_addresses.repository.DeliveryAddressRepository;
import br.psi.giganet.api.purchase.delivery_addresses.service.DeliveryAddressesService;
import br.psi.giganet.api.purchase.employees.model.Employee;
import br.psi.giganet.api.purchase.employees.repository.EmployeeRepository;
import br.psi.giganet.api.purchase.locations.model.Location;
import br.psi.giganet.api.purchase.locations.repository.LocationRepository;
import br.psi.giganet.api.purchase.payment_conditions.model.PaymentCondition;
import br.psi.giganet.api.purchase.payment_conditions.repository.PaymentConditionRepository;
import br.psi.giganet.api.purchase.products.categories.model.Category;
import br.psi.giganet.api.purchase.products.categories.repository.ProductCategoryRepository;
import br.psi.giganet.api.purchase.products.model.Product;
import br.psi.giganet.api.purchase.products.repository.ProductRepository;
import br.psi.giganet.api.purchase.projects.model.Project;
import br.psi.giganet.api.purchase.projects.repository.ProjectRepository;
import br.psi.giganet.api.purchase.purchase_order.model.*;
import br.psi.giganet.api.purchase.purchase_order.repository.PurchaseOrderRepository;
import br.psi.giganet.api.purchase.purchase_requests.model.PurchaseRequest;
import br.psi.giganet.api.purchase.purchase_requests.model.PurchaseRequestItem;
import br.psi.giganet.api.purchase.purchase_requests.repository.PurchaseRequestRepository;
import br.psi.giganet.api.purchase.quotation_approvals.model.QuotationApproval;
import br.psi.giganet.api.purchase.quotation_approvals.repository.QuotationApprovalRepository;
import br.psi.giganet.api.purchase.quotations.model.*;
import br.psi.giganet.api.purchase.quotations.model.enums.FreightType;
import br.psi.giganet.api.purchase.quotations.repository.QuotationRepository;
import br.psi.giganet.api.purchase.suppliers.model.Supplier;
import br.psi.giganet.api.purchase.suppliers.repository.SupplierRepository;
import br.psi.giganet.api.purchase.suppliers.taxes.model.Tax;
import br.psi.giganet.api.purchase.suppliers.taxes.repository.TaxRepository;
import br.psi.giganet.api.purchase.units.model.Unit;
import br.psi.giganet.api.purchase.units.repository.UnitRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;


@SpringBootTest
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@ActiveProfiles("test")
public class BuilderIntegrationTest extends DocsDescriptions {

    protected MockMvc mockMvc;
    protected ObjectMapper objectMapper = new ObjectMapper();
    protected Employee currentLoggedUser;

    protected UnitRepository unitRepository;
    protected ProductCategoryRepository productCategoryRepository;
    protected SupplierRepository supplierRepository;
    protected ProductRepository productRepository;
    protected EmployeeRepository employeeRepository;
    protected PurchaseRequestRepository purchaseRequestRepository;
    protected PermissionRepository permissionRepository;
    protected ApprovalRepository approvalRepository;
    protected QuotationRepository quotationRepository;
    protected QuotationApprovalRepository quotationApprovalRepository;
    protected PurchaseOrderRepository purchaseOrderRepository;
    protected CostCenterRepository costCenterRepository;
    protected PaymentConditionRepository paymentConditionRepository;
    protected DeliveryAddressesService addressService;
    protected TaxRepository taxRepository;
    protected DeliveryAddressRepository deliveryAddressRepository;
    protected NotificationRepository notificationRepository;
    protected BranchOfficeRepository branchOfficeRepository;
    protected ProjectRepository projectRepository;
    protected LocationRepository locationRepository;

    @BeforeEach
    public void mockMvcConfig(WebApplicationContext webApplicationContext,
                              RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .addFilter(((req, res, filterChain) -> {
                    req.setCharacterEncoding("UTF-8");
                    filterChain.doFilter(req, res);
                }))
                .build();

    }

    protected QuotationApproval createAndSaveQuotationApproval() {
        int value = getRandomId();

        QuotationApproval approval = new QuotationApproval();
        approval.setEvaluation(ProcessStatus.PENDING);
        approval.setNote("Aprovação de cotação de teste " + value);
        approval.setQuotation(createAndSaveQuotation());
        approval.setResponsible(createAndSaveEmployee());

        return quotationApprovalRepository.saveAndFlush(approval);
    }

    protected PurchaseOrder createAndSavePurchaseOrder() {
        int value = getRandomId();
        final var order = new PurchaseOrder();
        order.setStatus(ProcessStatus.PENDING);
        order.setApproval(createAndSaveQuotationApproval());
        order.setNote("Observacao de teste " + value);
        order.setExternalLink("https://link-externo.com.br");
        order.setResponsible(createAndSaveEmployee());

        order.setCostCenter(createAndSaveCostCenter());
        order.setBranchOffice(order.getApproval().getQuotation().getBranchOffice());
        order.setDateOfNeed(LocalDate.now().plusMonths(1));

        order.setProject(order.getApproval().getQuotation().getProject());
        order.setLocation(order.getApproval().getQuotation().getLocation());

        order.setPaymentCondition(new OrderPaymentCondition());
        order.getPaymentCondition().setOrder(order);
        order.getPaymentCondition().setCondition(createAndSavePaymentCondition());

        OrderConditionDueDate dueDate = new OrderConditionDueDate();
        dueDate.setCondition(order.getPaymentCondition());
        dueDate.setDueDate(LocalDate.now());
        dueDate.setCondition(order.getPaymentCondition());
        order.getPaymentCondition().setDueDates(new ArrayList<>());
        order.getPaymentCondition().getDueDates().add(dueDate);

        order.setFreight(new PurchaseOrderFreight());
        order.getFreight().setPrice(BigDecimal.TEN);
        order.getFreight().setType(FreightType.FOB);
        order.getFreight().setOrder(order);
        order.getFreight().setDeliveryAddress(addressService.getDeliveryAddressDefault().getAddress());
        order.getFreight().setDeliveryDate(ZonedDateTime.now().plusDays(20));

        order.setSupplier(createAndSaveSupplier());

//        order.setItems(new ArrayList<>());
//
//        for (int i = 0; i < 3; i++) {
//            final var item = new PurchaseOrderItem();
//            item.setOrder(order);
//            item.setStatus(ProcessStatus.PENDING);
//            item.setProduct(createAndSaveProduct());
//            item.setSupplier(order.getSupplier());
//            item.setQuantity(2d * (i + 1));
//            item.setIpi(4f);
//            item.setIcms(18f);
//            item.setUnit(createAndSaveUnit());
//            item.setPrice(BigDecimal.valueOf(1.5 * getRandomId() + 1));
//            item.setTotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
//            order.getItems().add(item);
//        }
        order.setItems(order.getApproval().getQuotation()
                .getItems().stream()
                .map(quotedItem -> {
                    final var item = new PurchaseOrderItem();
                    item.setOrder(order);
                    item.setStatus(ProcessStatus.PENDING);
                    item.setProduct(quotedItem.getProduct());
                    item.setSupplier(order.getSupplier());
                    item.setQuantity(quotedItem.getQuantity());
                    item.setIpi(quotedItem.getSelectedSupplier().getIpi());
                    item.setIcms(quotedItem.getSelectedSupplier().getIcms());
                    item.setUnit(quotedItem.getUnit());
                    item.setPrice(quotedItem.getSelectedSupplier().getPrice());
                    item.setTotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));

                    return item;
                })
                .collect(Collectors.toList()));

        order.setTotal(
                order.getItems().stream()
                        .map(PurchaseOrderItem::getTotal)
                        .reduce(BigDecimal::add)
                        .orElse(BigDecimal.ZERO)
        );

        order.setCompetencies(new ArrayList<>());
        order.getCompetencies().add(new PurchaseOrderCompetence(
                order,
                order.getCostCenter(),
                "NF " + getRandomId(),
                LocalDate.of(LocalDate.now().getYear(), LocalDate.now().getMonth(), 1).plusMonths(1),
                order.getTotal()
        ));

        return purchaseOrderRepository.saveAndFlush(order);
    }

    protected Quotation createAndSaveQuotation() {
        int value = getRandomId();

        Quotation quotation = new Quotation();
        quotation.setNote("Cotação de teste " + value);
        quotation.setStatus(ProcessStatus.REALIZED);
        quotation.setResponsible(createAndSaveEmployee());
        quotation.setCostCenter(createAndSaveCostCenter());
        quotation.setBranchOffice(createAndSaveBranchOffice());
        quotation.setDateOfNeed(LocalDate.now().plusMonths(1));
        quotation.setDescription("Descrição da cotação");
        quotation.setExternalLink("https://link-externo.com.br");

        quotation.setPaymentCondition(new QuotationPaymentCondition());
        quotation.getPaymentCondition().setQuotation(quotation);
        quotation.getPaymentCondition().setCondition(createAndSavePaymentCondition());
        QuotationConditionDueDate dueDate = new QuotationConditionDueDate();
        dueDate.setCondition(quotation.getPaymentCondition());
        dueDate.setDueDate(LocalDate.now());
        quotation.getPaymentCondition().setDueDates(Collections.singletonList(dueDate));

        quotation.setFreight(new QuotationFreight());
        quotation.getFreight().setPrice(BigDecimal.TEN);
        quotation.getFreight().setType(FreightType.FOB);

        quotation.setLocation(createAndSaveLocation(quotation.getBranchOffice()));
        quotation.setProject(createAndSaveProject());

        quotation.setItems(new ArrayList<>());

        final Approval approval = createAndSaveApproval();
        for (int i = 0; i < approval.getItems().size(); i++) {
            final QuotedItem item = new QuotedItem();
            item.setQuotation(quotation);

            // with approval
            final ApprovalItem approvalItem = approval.getItems().get(i);
            item.setApprovedItem(approvalItem);
            item.setProduct(approvalItem.getItem().getProduct());
            item.setUnit(approvalItem.getItem().getUnit());

            item.setQuantity(15d * (i + 1));
            item.setStatus(ProcessStatus.PENDING);
            item.setSuppliers(new ArrayList<>());

            for (int j = 0; j < 2; j++) {
                var s = new SupplierItemQuotation();
                s.setQuotedItem(item);
                s.setSupplier(createAndSaveSupplier());
                s.setQuantity(item.getQuantity());
                s.setUnit(item.getUnit());
                s.setIcms(18f);
                s.setIpi(4f);
                s.setPrice(BigDecimal.valueOf(150 * getRandomId() + 1));
                s.setTotal(s.getPrice().multiply(BigDecimal.valueOf(s.getQuantity())));
                s.setIsSelected(Boolean.FALSE);
                item.getSuppliers().add(s);
            }
            item.getSuppliers().get(0).setIsSelected(Boolean.TRUE);
            quotation.getItems().add(item);
        }

        // independent items
        for (int i = 0; i < 1; i++) {
            final QuotedItem item = new QuotedItem();
            item.setQuotation(quotation);
            item.setProduct(createAndSaveProduct());
            item.setQuantity(15d * (i + 1));
            item.setStatus(ProcessStatus.PENDING);
            item.setSuppliers(new ArrayList<>());
            item.setUnit(createAndSaveUnit());

            for (int j = 0; j < 2; j++) {
                var s = new SupplierItemQuotation();
                s.setQuotedItem(item);
                s.setSupplier(createAndSaveSupplier());
                s.setQuantity(item.getQuantity());
                s.setUnit(createAndSaveUnit());
                s.setPrice(BigDecimal.valueOf(150 * getRandomId() + 1));
                s.setIcms(18f);
                s.setIpi(4f);
                s.setTotal(s.getPrice().multiply(BigDecimal.valueOf(s.getQuantity())));
                s.setIsSelected(Boolean.FALSE);
                item.getSuppliers().add(s);
            }
            item.getSuppliers().get(0).setIsSelected(Boolean.TRUE);
            quotation.getItems().add(item);
        }

        quotation.setTotal(
                quotation.getItems().stream()
                        .map(q -> q.getSelectedSupplier().getTotal())
                        .reduce(BigDecimal::add)
                        .orElse(BigDecimal.ZERO)
                        .add(quotation.getFreight().getPrice()));

        return quotationRepository.saveAndFlush(quotation);
    }

    protected Supplier createAndSaveSupplier() {
        int value = getRandomId();

        Supplier request = new Supplier();
        request.setName("Fornecedor " + value);
        request.setEmail("email@email.com");
        request.setCellphone("12341234");
        request.setTelephone("12341234");
        request.setCnpj("55191816000131");
        request.setDescription("Descrição de teste " + value);
        request.setAddress(new Address());
        request.getAddress().setStreet("Rua teste " + value);
        request.getAddress().setNumber("120");
        request.getAddress().setDistrict("Horto");
        request.getAddress().setCity("Ipatinga");
        request.getAddress().setState("MG");
        request.setTax(createAndSaveTax());
        return supplierRepository.saveAndFlush(request);
    }

    protected Tax createAndSaveTax() {
        Tax tax = new Tax();
        tax.setIcms(18f);
        tax.setStateTo("MG");
        tax.setStateFrom("MG");

        return taxRepository.findByStateFrom("MG").orElseGet(() -> taxRepository.save(tax));
    }

    protected Unit createAndSaveUnit() {
        int value = getRandomId();

        Unit unit = new Unit();
        unit.setName("Unit  " + value);
        unit.setAbbreviation("abbrev  " + value);
        unit.setDescription("Unidade de teste " + value);
        return unitRepository.saveAndFlush(unit);
    }

    protected CostCenter createAndSaveCostCenter() {
        int value = getRandomId();

        CostCenter costCenter = new CostCenter();
        costCenter.setName("Centro de custo  " + value);
        costCenter.setDescription("Centro de custo de teste " + value);
        return costCenterRepository.saveAndFlush(costCenter);
    }

    protected DeliveryAddress createAndSaveDeliveryAddress() {
        int value = getRandomId();

        DeliveryAddress deliveryAddress = new DeliveryAddress();
        deliveryAddress.setName("Endereço de entrega  " + value);
        deliveryAddress.setAddress(new Address());
        deliveryAddress.getAddress().setStreet("Rua teste " + value);
        deliveryAddress.getAddress().setNumber("120");
        deliveryAddress.getAddress().setDistrict("Horto");
        deliveryAddress.getAddress().setCity("Ipatinga");
        deliveryAddress.getAddress().setState("MG");
        return deliveryAddressRepository.saveAndFlush(deliveryAddress);
    }

    protected Project createAndSaveProject() {
        Project project = new Project();
        project.setName("Projeto " + getRandomId());
        project.setDescription("Descrição do projeto de teste");
        return projectRepository.saveAndFlush(project);
    }

    protected Location createAndSaveLocation() {
        return createAndSaveLocation(createAndSaveBranchOffice());
    }

    protected Location createAndSaveLocation(BranchOffice branchOffice) {
        Location location = new Location();
        location.setName("Localidade " + getRandomId());
        location.setDescription("Descrição da localidade de teste");
        location.setBranchOffice(branchOffice);
        return locationRepository.saveAndFlush(location);
    }

    protected PaymentCondition createAndSavePaymentCondition() {
        int value = getRandomId();

        PaymentCondition paymentCondition = new PaymentCondition();
        paymentCondition.setName("Centro de custo  " + value);
        paymentCondition.setDescription("Centro de custo de teste " + value);
        paymentCondition.setNumberOfInstallments(10);
        paymentCondition.setDaysInterval(30);
        return paymentConditionRepository.saveAndFlush(paymentCondition);
    }

    protected Category createAndSaveCategory() {
        int value = getRandomId();

        Category category = new Category();
        category.setName("Categoria  " + value);
        category.setPattern("1XXXX");
        category.setDescription("Categoria de teste " + value);
        return productCategoryRepository.saveAndFlush(category);
    }

    protected Product createAndSaveProduct() {
        int value = getRandomId();

        Product product = new Product();
        product.setName("Product " + value);
        product.setCategory(createAndSaveCategory());
        product.setCode(product.getCategory().getPattern().replaceAll("X", "") + value);
        product.setUnit(createAndSaveUnit());
        product.setManufacturer("Fabricante " + value);
        product.setDescription("Produto de teste " + value);
        return productRepository.saveAndFlush(product);
    }

    protected PurchaseRequest createAndSavePurchaseRequest() {
        int value = getRandomId();

        PurchaseRequest p = new PurchaseRequest();
        p.setRequester(createAndSaveEmployee());
        p.setResponsible(createAndSaveEmployee());
        p.setReason("Motivo de teste");
        p.setDescription("Descrição de teste");
        p.setDateOfNeed(LocalDate.now());
        p.setNote("Purchase de teste " + value);
        p.setStatus(ProcessStatus.PENDING);
        p.setItems(new ArrayList<>());
        p.setCostCenter(createAndSaveCostCenter());
        p.setBranchOffice(createAndSaveBranchOffice());

        for (int i = 0; i < 3; i++) {
            final PurchaseRequestItem item = new PurchaseRequestItem();
            item.setProduct(createAndSaveProduct());
            item.setQuantity(10d * (i + 1));
            item.setPurchaseRequest(p);
            item.setStatus(ProcessStatus.PENDING);
            item.setUnit(item.getProduct().getUnit());
            p.getItems().add(item);
        }

        return purchaseRequestRepository.saveAndFlush(p);
    }

    protected Approval createAndSaveApproval() {
        int value = getRandomId();

        Approval approval = new Approval();
        approval.setRequest(createAndSavePurchaseRequest());
        approval.setResponsible(approval.getRequest().getResponsible());
        approval.setStatus(ProcessStatus.PENDING);
        approval.setNote("Approval de teste " + value);
        approval.setItems(
                approval.getRequest()
                        .getItems()
                        .stream()
                        .map(i -> {
                            ApprovalItem item = new ApprovalItem();
                            item.setApproval(approval);
                            item.setEvaluation(ProcessStatus.PENDING);
                            item.setItem(i);
                            return item;
                        })
                        .collect(Collectors.toList())
        );

        return approvalRepository.saveAndFlush(approval);
    }

    protected BranchOffice createAndSaveBranchOffice() {
        BranchOffice office = new BranchOffice();
        office.setName("Giganet Ipatinga");
        office.setShortName("Ipa");
        office.setAddress(new Address());
        office.getAddress().setStreet("Rua teste " + getRandomId());
        office.getAddress().setNumber("120");
        office.getAddress().setDistrict("Horto");
        office.getAddress().setPostalCode("35160294");
        office.getAddress().setCity("Ipatinga");
        office.getAddress().setState("MG");
        office.setTelephone("12341234");
        office.setStateRegistration("12341235899");
        office.setCnpj("55191816000131");

        return branchOfficeRepository.saveAndFlush(office);
    }

    protected Employee createAndSaveEmployee() {
        int value = getRandomId();

        Employee employee = new Employee();
        employee.setName("Employee " + value);
        employee.setEmail("employee" + value + "@email.com");
        employee.setPassword(new BCryptPasswordEncoder().encode("123456"));
        employee.setPermissions(new HashSet<>(Collections.singletonList(createAndSavePermission())));

        return employeeRepository.findByEmail(employee.getEmail())
                .orElseGet(() -> employeeRepository.saveAndFlush(employee));
    }

    protected Employee createAndSaveEmployee(String email) {
        int value = getRandomId();

        Employee employee = new Employee();
        employee.setName("Employee " + value);
        employee.setEmail(email);
        employee.setPassword(new BCryptPasswordEncoder().encode("123456"));
        employee.setPermissions(new HashSet<>(Arrays.asList(
                createAndSavePermission("ROLE_ADMIN"),
                createAndSavePermission("ROLE_ROOT")
        )));

        return employeeRepository.findByEmail(employee.getEmail())
                .orElseGet(() -> employeeRepository.saveAndFlush(employee));
    }

    protected Permission createAndSavePermission() {
        final String permission = "ROLE_ADMIN";
        return permissionRepository.findById(permission)
                .orElseGet(() -> permissionRepository.saveAndFlush(new Permission(permission)));
    }

    protected Permission createAndSavePermission(String permission) {
        return permissionRepository.findById(permission)
                .orElseGet(() -> permissionRepository.saveAndFlush(new Permission(permission)));
    }

    protected void createCurrentUser() {
        try {
            if (currentLoggedUser == null) {
                currentLoggedUser = createAndSaveEmployee("teste@teste.com");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void createNotificationPermissions() {
        createAndSavePermission("ROLE_NOTIFICATIONS");
        createAndSavePermission("ROLE_NOTIFICATIONS_NEW_QUOTATION_APPROVAL");
        createAndSavePermission("ROLE_NOTIFICATIONS_EVALUATE_QUOTATION_APPROVAL");
        createAndSavePermission("ROLE_NOTIFICATIONS_EVALUATE_PURCHASE_REQUEST_APPROVAL");
        createAndSavePermission("ROLE_NOTIFICATIONS_RECEIVE_PURCHASE_ORDER_ITEMS");
    }

    protected Notification createAndSaveNotification() {
        return createAndSaveNotification(employeeRepository.findAll());
    }

    protected Notification createAndSaveNotification(List<Employee> employees) {
        Notification notification = new Notification();
        notification.setTitle("Teste");
        notification.setDescription("NOtificação de teste");
        notification.setType(NotificationType.NEW_QUOTATION_APPROVAL);
        notification.setData(UUID.randomUUID().toString());
        notification.setRoles(Collections.singletonList(new NotificationRole(
                createAndSavePermission("ROLE_NOTIFICATIONS"),
                notification
        )));
        notification.setEmployees(employees.stream()
                .map(employee -> new NotificationEmployee(employee, notification, Boolean.FALSE))
                .collect(Collectors.toList()));

        return notificationRepository.saveAndFlush(notification);
    }

    protected int getRandomId() {
        final Random r = new Random();
        return r.nextInt(1000000);
    }

}
