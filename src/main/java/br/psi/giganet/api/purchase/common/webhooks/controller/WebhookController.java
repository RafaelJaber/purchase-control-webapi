package br.psi.giganet.api.purchase.common.webhooks.controller;

import br.psi.giganet.api.purchase.common.webhooks.controller.request.EmitWebhookRequest;
import br.psi.giganet.api.purchase.common.webhooks.model.Webhook;
import br.psi.giganet.api.purchase.common.webhooks.services.WebhooksHandlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/webhooks")
public class WebhookController {

    @Autowired
    private WebhooksHandlerService webHookService;

    @PostMapping
    public ResponseEntity<Object> webHookReceive(
            @RequestHeader("Signature") String signature,
            @Valid @RequestBody Webhook webHook) {
        this.webHookService.onReceiveWebHookHandler(webHook, signature);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/emit")
    public ResponseEntity<Object> webHookEmit(
            @Valid @RequestBody EmitWebhookRequest request) {
        this.webHookService.emitWebhook(request.getType(), request.getId());
        return ResponseEntity.noContent().build();
    }

}
