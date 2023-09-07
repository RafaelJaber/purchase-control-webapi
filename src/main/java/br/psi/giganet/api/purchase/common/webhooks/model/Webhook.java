package br.psi.giganet.api.purchase.common.webhooks.model;

import lombok.Data;

@Data
public class Webhook {

    private String id;
    private WebhookServer origin;
    private WebhookType type;
    private Object data;

}
