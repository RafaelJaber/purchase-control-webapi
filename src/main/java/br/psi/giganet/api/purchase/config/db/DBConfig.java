package br.psi.giganet.api.purchase.config.db;

import br.psi.giganet.api.purchase.approvals.repository.ApprovalItemRepository;
import br.psi.giganet.api.purchase.approvals.repository.ApprovalRepository;
import br.psi.giganet.api.purchase.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.api.purchase.common.notifications.repository.NotificationRepository;
import br.psi.giganet.api.purchase.common.settings.repository.SettingRepository;
import br.psi.giganet.api.purchase.config.audit.AuditorAwareImpl;
import br.psi.giganet.api.purchase.config.security.repository.AbstractUserRepository;
import br.psi.giganet.api.purchase.config.security.repository.PermissionRepository;
import br.psi.giganet.api.purchase.cost_center.repository.CostCenterRepository;
import br.psi.giganet.api.purchase.delivery_addresses.repository.DeliveryAddressRepository;
import br.psi.giganet.api.purchase.employees.repository.EmployeeRepository;
import br.psi.giganet.api.purchase.locations.repository.LocationRepository;
import br.psi.giganet.api.purchase.payment_conditions.repository.PaymentConditionRepository;
import br.psi.giganet.api.purchase.products.categories.repository.ProductCategoryRepository;
import br.psi.giganet.api.purchase.products.repository.ProductRepository;
import br.psi.giganet.api.purchase.projects.repository.ProjectRepository;
import br.psi.giganet.api.purchase.purchase_order.repository.PurchaseOrderItemRepository;
import br.psi.giganet.api.purchase.purchase_order.repository.PurchaseOrderRepository;
import br.psi.giganet.api.purchase.purchase_requests.repository.PurchaseRequestItemsRepository;
import br.psi.giganet.api.purchase.purchase_requests.repository.PurchaseRequestRepository;
import br.psi.giganet.api.purchase.quotation_approvals.repository.QuotationApprovalRepository;
import br.psi.giganet.api.purchase.quotations.repository.QuotationRepository;
import br.psi.giganet.api.purchase.quotations.repository.SupplierItemQuotationRepository;
import br.psi.giganet.api.purchase.suppliers.repository.SupplierRepository;
import br.psi.giganet.api.purchase.suppliers.taxes.repository.TaxRepository;
import br.psi.giganet.api.purchase.units.repository.UnitRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.time.ZonedDateTime;
import java.util.Optional;

@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "dateTimeProvider")
@EnableJpaRepositories(basePackageClasses = {
        AbstractUserRepository.class,
        PermissionRepository.class,
        ProductRepository.class,
        SupplierRepository.class,
        PurchaseRequestRepository.class,
        EmployeeRepository.class,
        ApprovalRepository.class,
        ApprovalItemRepository.class,
        QuotationRepository.class,
        SupplierItemQuotationRepository.class,
        QuotationApprovalRepository.class,
        PurchaseOrderRepository.class,
        PurchaseOrderItemRepository.class,
        UnitRepository.class,
        ProductCategoryRepository.class,
        CostCenterRepository.class,
        PaymentConditionRepository.class,
        TaxRepository.class,
        PurchaseRequestItemsRepository.class,
        DeliveryAddressRepository.class,
        NotificationRepository.class,
        BranchOfficeRepository.class,
        LocationRepository.class,
        ProjectRepository.class,
        SettingRepository.class
})

public class DBConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return new AuditorAwareImpl();
    }

    @Bean
    public DateTimeProvider dateTimeProvider() {
        return () -> Optional.of(ZonedDateTime.now());
    }
}
