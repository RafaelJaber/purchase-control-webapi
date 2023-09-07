package br.psi.giganet.api.purchase.common.webhooks.controller.request;

import br.psi.giganet.api.purchase.common.webhooks.model.WebhookType;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class EmitWebhookRequest {

    @NotNull(message = "Tipo do webhook não pode ser nulo")
    private WebhookType type;
    private Long id;

}
