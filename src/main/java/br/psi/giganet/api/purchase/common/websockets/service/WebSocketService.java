package br.psi.giganet.api.purchase.common.websockets.service;

import br.psi.giganet.api.purchase.common.notifications.model.Notification;
import br.psi.giganet.api.purchase.common.notifications.model.NotificationEmployee;
import br.psi.giganet.api.purchase.config.security.model.User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WebSocketService extends AbstractWebSocketHandler {

    public void send(Notification notification) {
        List<? extends User> users = notification.getEmployees().stream()
                .map(NotificationEmployee::getEmployee)
                .collect(Collectors.toList());

        users.forEach(user -> send(
                "/topic/notifications/" + user.getId(),
                Collections.singletonMap("notification", notification.getId())));
    }

}
