package br.psi.giganet.api.purchase.common.notifications.service;

import br.psi.giganet.api.purchase.approvals.model.Approval;
import br.psi.giganet.api.purchase.common.notifications.factory.NotificationFactory;
import br.psi.giganet.api.purchase.common.notifications.model.Notification;
import br.psi.giganet.api.purchase.common.notifications.model.NotificationEmployee;
import br.psi.giganet.api.purchase.common.notifications.repository.NotificationRepository;
import br.psi.giganet.api.purchase.common.websockets.service.WebSocketService;
import br.psi.giganet.api.purchase.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.api.purchase.config.security.service.PermissionService;
import br.psi.giganet.api.purchase.employees.model.Employee;
import br.psi.giganet.api.purchase.employees.service.EmployeeService;
import br.psi.giganet.api.purchase.purchase_order.model.PurchaseOrder;
import br.psi.giganet.api.purchase.quotation_approvals.model.QuotationApproval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private NotificationFactory notificationFactory;

    public Optional<Notification> findById(Long id) {
        return notificationRepository.findById(id);
    }

    public List<Notification> findAllByEmployee() {
        return findAllByEmployee(employeeService.getCurrentLoggedEmployee()
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado")));
    }

    public List<Notification> findAllByEmployee(Employee employee) {
        return notificationRepository.findAllByEmployee(
                employeeService.findById(employee.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado")));
    }

    public List<Notification> findAllUnreadByEmployee() {
        return findAllUnreadByEmployee(employeeService.getCurrentLoggedEmployee()
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado")));
    }

    public List<Notification> findAllUnreadByEmployee(Employee employee) {
        return notificationRepository.findAllUnreadByEmployee(
                employeeService.findById(employee.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado")));
    }

    public Optional<Notification> markAsViewed(Notification notification) {
        return markAsViewed(notification, employeeService.getCurrentLoggedEmployee()
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado")));
    }

    @Transactional
    public List<Notification> markAsViewed(List<Notification> notifications) {
        Employee currentEmployee = employeeService.getCurrentLoggedEmployee()
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado"));

        return notifications.stream()
                .map(notification -> markAsViewed(notification, currentEmployee)
                        .orElseThrow(() -> new IllegalArgumentException("Não foi possível marcar a notificação " +
                                notification.getTitle() + " como lida")))
                .collect(Collectors.toList());
    }

    public Optional<Notification> markAsViewed(Notification notification, Employee employee) {
        return notificationRepository.findById(notification.getId())
                .filter(saved -> saved.getEmployees().stream().anyMatch(e -> e.getEmployee().equals(employee)))
                .map(saved -> {
                    final NotificationEmployee notificationEmployee = saved.getEmployees().stream()
                            .filter(e -> e.getEmployee().equals(employee))
                            .findFirst()
                            .get();

                    notificationEmployee.setViewed(Boolean.TRUE);
                    return notificationRepository.save(saved);

                });
    }

    public void onCreateQuotationApproval(QuotationApproval approval) {
        Notification notification = handleOnCreate(notificationFactory.createOnNewQuotationApproval(approval));
        webSocketService.send(notification);
    }

    public void onEvaluateQuotationApproval(QuotationApproval approval) {
        Notification notification = handleOnCreate(notificationFactory.createOnEvaluateQuotationApproval(approval));
        webSocketService.send(notification);
    }

    public void onEvaluateApproval(Approval approval) {
        Notification notification = handleOnCreate(notificationFactory.createOnEvaluateApproval(approval));
        webSocketService.send(notification);
    }

    public void onUpdatePurchaseOrderByEntry(PurchaseOrder order) {
        Notification notification = handleOnCreate(notificationFactory.createOnUpdatePurchaseOrderByEntry(order));
        webSocketService.send(notification);
    }

    private Notification handleOnCreate(Notification notification) {
        notification.getRoles().forEach(role -> {

            role.setPermission(permissionService.findById(role.getPermission().getName())
                    .orElseThrow(() -> new IllegalArgumentException("Permissão não encontrada")));

            notification.getEmployees().addAll(
                    employeeService.findByPermission(role.getPermission())
                            .stream()
                            .filter(employee -> notification.getEmployees().stream()
                                    .noneMatch(e -> e.getEmployee().equals(employee)))
                            .map(employee -> new NotificationEmployee(employee, notification, Boolean.FALSE))
                            .collect(Collectors.toList()));
        });
        return notificationRepository.save(notification);
    }

}
