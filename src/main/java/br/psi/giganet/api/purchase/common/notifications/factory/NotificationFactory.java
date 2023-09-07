package br.psi.giganet.api.purchase.common.notifications.factory;

import br.psi.giganet.api.purchase.approvals.model.Approval;
import br.psi.giganet.api.purchase.common.notifications.model.Notification;
import br.psi.giganet.api.purchase.common.notifications.model.NotificationRole;
import br.psi.giganet.api.purchase.common.notifications.model.NotificationType;
import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.config.security.model.Permission;
import br.psi.giganet.api.purchase.purchase_order.model.PurchaseOrder;
import br.psi.giganet.api.purchase.quotation_approvals.model.QuotationApproval;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;

@Component
public class NotificationFactory {


    public Notification create(Long id) {
        Notification notification = new Notification();
        notification.setId(id);

        return notification;
    }

    public Notification createOnNewQuotationApproval(QuotationApproval approval) {
        Notification notification = new Notification();
        notification.setTitle("Aprovação de Cotação");
        notification.setDescription("A cotação " + approval.getQuotation().getId() + " esta aguardando pela aprovação");
        notification.setData(approval.getId().toString());
        notification.setType(NotificationType.NEW_QUOTATION_APPROVAL);
        notification.setRoles(Collections.singletonList(
                new NotificationRole(new Permission("ROLE_NOTIFICATIONS_NEW_QUOTATION_APPROVAL"), notification)));
        notification.setEmployees(new ArrayList<>());

        return notification;
    }

    public Notification createOnEvaluateQuotationApproval(QuotationApproval approval) {
        Notification notification = new Notification();
        notification.setTitle("Aprovação de Cotação");
        notification.setDescription("A cotação " + approval.getQuotation().getId() + " foi " +
                (approval.getEvaluation().equals(ProcessStatus.APPROVED) ? " aprovada" : "rejeitada"));
        notification.setData(approval.getId().toString());
        notification.setType(NotificationType.EVALUATE_QUOTATION_APPROVAL);
        notification.setRoles(Collections.singletonList(
                new NotificationRole(new Permission("ROLE_NOTIFICATIONS_EVALUATE_QUOTATION_APPROVAL"), notification)));
        notification.setEmployees(new ArrayList<>());

        return notification;
    }

    public Notification createOnEvaluateApproval(Approval approval) {
        Notification notification = new Notification();
        notification.setTitle("Aprovação de Solicitação");
        notification.setDescription("A solicitação " + approval.getRequest().getId() + " foi " +
                (approval.getStatus().equals(ProcessStatus.APPROVED) ? " aprovada" : "rejeitada"));
        notification.setData(approval.getId().toString());
        notification.setType(NotificationType.EVALUATE_PURCHASE_REQUEST_APPROVAL);
        notification.setRoles(Collections.singletonList(
                new NotificationRole(new Permission("ROLE_NOTIFICATIONS_EVALUATE_PURCHASE_REQUEST_APPROVAL"), notification)));
        notification.setEmployees(new ArrayList<>());

        return notification;
    }

    public Notification createOnUpdatePurchaseOrderByEntry(PurchaseOrder order) {
        Notification notification = new Notification();
        notification.setTitle("Ordem de Compra");
        notification.setDescription("A ordem de compra " + order.getId() + " foi " +
                (order.getStatus().equals(ProcessStatus.RECEIVED) ? "recebida" : "parcialmente recebida"));
        notification.setData(order.getId().toString());
        notification.setType(NotificationType.RECEIVE_PURCHASE_ORDER_ITEMS);
        notification.setRoles(Collections.singletonList(
                new NotificationRole(new Permission("ROLE_NOTIFICATIONS_RECEIVE_PURCHASE_ORDER_ITEMS"), notification)));
        notification.setEmployees(new ArrayList<>());

        return notification;
    }

}
